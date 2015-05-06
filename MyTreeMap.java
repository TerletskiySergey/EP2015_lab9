package EPAM2015_lab9;

import java.util.*;

public class MyTreeMap implements MyMap {

    private static final boolean BLACK = true;
    private static final boolean RED = false;

    static class SimpleEntry implements Entry {

        private SimpleEntry parent;
        private SimpleEntry left;
        private SimpleEntry right;
        private Object key;
        private Object value;
        private boolean color;

        public SimpleEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
            this.color = RED;
        }

        public void changeColor() {
            this.color = !this.color;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            Object toReturn = this.value;
            this.value = value;
            return toReturn;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Entry &&
                    this.key.equals(((Entry) o).getKey()) &&
                    this.value.equals(((Entry) o).getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return this.key.toString() + (this.color ? "(b)" : "(r)");
        }
    }

    private class EntryIterator implements Iterator<MyMap.Entry> {

        private SimpleEntry nextEntry;
        private int expectedModCount = modCount;

        private EntryIterator() {
            this.nextEntry = minimal(root);
        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public Entry next() {
            if (this.nextEntry == null) {
                throw new NoSuchElementException();
            }
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            SimpleEntry toReturn = nextEntry;
            nextEntry = successor(nextEntry);
            return toReturn;
        }
    }

    private SimpleEntry root;
    private Comparator comparator;
    private int size;
    private int modCount;

    public MyTreeMap() {
        this(null);
    }

    public MyTreeMap(Comparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return findEntry(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Iterator<Entry> iterator = new EntryIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        SimpleEntry entry = findEntry(key);
        return entry == null ? null : entry.value;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Object put(Object key, Object value) {
        SimpleEntry curEntry = findInsertPoint(key);
        if (curEntry == null) {
            root = new SimpleEntry(key, value);
            root.color = BLACK;
            size++;
            modCount++;
            return null;
        }
        if (curEntry.key.equals(key)) {
            Object toReturn = curEntry.value;
            curEntry.value = value;
            return toReturn;
        }
        SimpleEntry toAdd = new SimpleEntry(key, value);
        toAdd.parent = curEntry;
        if (compare(curEntry.key, key) > 0) {
            curEntry.left = toAdd;
        } else {
            curEntry.right = toAdd;
        }
        size++;
        modCount++;
        fixAfterInsert(toAdd);
        return null;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator entryIterator() {
        return new EntryIterator();
    }

    private SimpleEntry findInsertPoint(Object key) {
        SimpleEntry curEntry = root;
        while (curEntry != null) {
            fixDuringInsert(curEntry);
            if (curEntry.key.equals(key)) {
                return curEntry;
            }
            if (compare(curEntry.key, key) > 0) {
                if (curEntry.left == null) {
                    return curEntry;
                } else {
                    curEntry = curEntry.left;
                }
            } else {
                if (curEntry.right == null) {
                    return curEntry;
                } else {
                    curEntry = curEntry.right;
                }
            }
        }
        return curEntry;
    }

    private SimpleEntry findEntry(Object key) {
        if (key == null) {
            return null;
        }
        SimpleEntry curElement = root;
        while (curElement != null && !curElement.key.equals(key)) {
            if (((Comparable) curElement.key).compareTo(key) > 0) {
                curElement = curElement.left;
            } else {
                curElement = curElement.right;
            }
        }
        return curElement;
    }

    private int compare(Object o1, Object o2) {
        return this.comparator == null
                ? ((Comparable) o1).compareTo(o2) : this.comparator.compare(o1, o2);
    }

    private static boolean isRight(SimpleEntry toCheck) {
        return toCheck.parent.right == toCheck;
    }

    // RBRules util methods

    private void fixDuringInsert(SimpleEntry toCheck) {
        if (hasRedSuccessors(toCheck)) {
            flipColor(toCheck);
        }
        if (toCheck != root && isDoubleRed(toCheck)) {
            pullUp(toCheck);
        }
    }

    private void fixAfterInsert(SimpleEntry toCheck) {
        if (isDoubleRed(toCheck)) {
            pullUp(toCheck);
        }
    }

    private boolean isDoubleRed(SimpleEntry toCheck) {
        if (toCheck.color == RED && toCheck.parent.color == RED) {
            return true;
        }
        return false;
    }

    private boolean hasRedSuccessors(SimpleEntry toCheck) {
        if (toCheck.left == null || toCheck.right == null) {
            return false;
        }
        if (toCheck.left.color == RED && toCheck.right.color == RED) {
            return true;
        }
        return false;
    }

    private boolean hasBlackSuccessors(SimpleEntry toCheck) {
        if (toCheck.left == null || toCheck.right == null) {
            return false;
        }
        if (toCheck.left.color == BLACK && toCheck.right.color == BLACK) {
            return true;
        }
        return false;
    }

    private void flipColor(SimpleEntry toFlip) {
        if (toFlip != root) {
            toFlip.changeColor();
        }
        toFlip.left.changeColor();
        toFlip.right.changeColor();
    }

    private void pullUp(SimpleEntry toPull) {
//        if (toPull.parent == null || toPull.parent.parent == null) {
//            throw new IllegalArgumentException("Tree element has no ancestor");
//        }
        if (isRight(toPull) ^ isRight(toPull.parent)) {
            // Inner grandson
            toPull.parent.parent.changeColor();
            toPull.changeColor();
            for (int i = 0; i < 2; i++) {
                if (isRight(toPull)) {
                    roL(toPull.parent);
                } else {
                    roR(toPull.parent);
                }
            }
        } else {
            // Outer grandson
            toPull.parent.parent.changeColor();
            toPull.parent.changeColor();
            if (isRight(toPull)) {
                roL(toPull.parent.parent);
            } else {
                roR(toPull.parent.parent);
            }
        }
    }

/*    private void pullDown(SimpleEntry toPull, Object toDelete) {
        // At least one red successor
        if (!hasBlackSuccessors(toPull)) {
            if (compare(toPull.key, toDelete) > 0 && toPull.left.color == BLACK) {
                toPull.changeColor();
                toPull.right.changeColor();
                roL(toPull);
            } else if (compare(toPull.key, toDelete) < 0 && toPull.right.color == BLACK) {
                toPull.changeColor();
                toPull.left.changeColor();
                roR(toPull);
            }
        } else if ((hasBlackSuccessors(toPull) && sibling(toPull)))
//        if (isRight(toPull)){
//            if (toPull.parent.left.color == )
//        }
    }*/

    private void roR(SimpleEntry top) {
        if (top == null) {
            return;
        }
        if (top.left == null) {
            throw new IllegalArgumentException("Unable to perform rotation right");
        }
        top.left.parent = top.parent;
        if (top == root) {
            root = top.left;
        } else {
            if (isRight(top)) {
                top.parent.right = top.left;
            } else {
                top.parent.left = top.left;
            }
        }
        SimpleEntry leftInnerG = top.left.right;
        top.left.right = top;
        if (leftInnerG != null) {
            leftInnerG.parent = top;
        }
        top.parent = top.left;
        top.left = leftInnerG;
    }

    private void roL(SimpleEntry top) {
        if (top == null) {
            return;
        }
        if (top.right == null) {
            throw new IllegalArgumentException("Unable to perform rotation left");
        }
        top.right.parent = top.parent;
        if (top == root) {
            root = top.right;
        } else {
            if (isRight(top)) {
                top.parent.right = top.right;
            } else {
                top.parent.left = top.right;
            }
        }
        SimpleEntry rightInnerG = top.right.left;
        top.right.left = top;
        if (rightInnerG != null) {
            rightInnerG.parent = top;
        }
        top.parent = top.right;
        top.right = rightInnerG;
    }

    private static SimpleEntry successor(SimpleEntry entry) {
        if (entry == null) {
            return null;
        }
        SimpleEntry curEntry = entry.right;
        if (curEntry == null) {
            curEntry = entry.parent;
            while (curEntry != null && curEntry.right == entry) {
                entry = curEntry;
                curEntry = curEntry.parent;
            }
            return curEntry;
        }
        while (curEntry.left != null) {
            curEntry = curEntry.left;
        }
        return curEntry;
    }

    private static SimpleEntry minimal(SimpleEntry root) {
        SimpleEntry curEntry = root;
        if (curEntry == null) {
            return null;
        }
        while (curEntry.left != null) {
            curEntry = curEntry.left;
        }
        return curEntry;
    }

    private static SimpleEntry sibling(SimpleEntry entry) {
        if (entry == null || entry.parent == null) {
            return null;
        }
        if (isRight(entry)) {
            return entry.parent.left;
        }
        return entry.parent.right;
    }

    public String toString() {

        if (this.root == null) return "";

        StringBuilder toReturn = new StringBuilder();

        Queue<SimpleEntry> q = new LinkedList<>();

        q.add(root);

        int maxLevel = getMaxLevel(root, 0);

        int maxKeySize = getLongestToString(root, 0) + 1;

        for (int i = 0; i <= maxLevel; i++) {

            int edgeOffset = (int) ((Math.pow(2, maxLevel - i - 1) - 1) * (maxKeySize + 1) + Math.ceil(maxKeySize / 2.0));

            toReturn.append(getSpace(' ', edgeOffset));

            int levelLength = (int) Math.pow(2, i);

            for (int j = 0; j < levelLength; j++) {

                SimpleEntry curEntry = q.poll();

                if (i != 0 && j % 2 == 0) toReturn.append('[');

                toReturn.append(curEntry == null ? getString("--", maxKeySize - 1) : getString(curEntry.toString(), maxKeySize - 1));

                if (i != 0 && j % 2 != 0) toReturn.append(']');

                if (j != levelLength - 1) {

                    int nodeOffset = (int) (Math.pow(2, maxLevel - i) * (maxKeySize + 1) - maxKeySize);

                    toReturn.append(j % 2 != 0 ? getSpace(' ', nodeOffset) : getSpace('_', nodeOffset));
                }

                q.add(curEntry == null || curEntry.left == null ? null : curEntry.left);

                q.add(curEntry == null || curEntry.right == null ? null : curEntry.right);

            }

            toReturn.append("\n");
        }

        toReturn.deleteCharAt(toReturn.length() - 1);

        return toReturn.toString();
    }

    private int getMaxLevel(SimpleEntry curEntry, int level) {

        if (curEntry == null) return level - 1;

        int leftLevel, rightLevel;

        leftLevel = getMaxLevel(curEntry.left, level + 1);

        rightLevel = getMaxLevel(curEntry.right, level + 1);

        return Math.max(level, Math.max(leftLevel, rightLevel));
    }

    private int getLongestToString(SimpleEntry curEntry, int maxSize) {

        if (curEntry == null) return maxSize;

        maxSize = Math.max(maxSize, curEntry.toString().length());

        int maxSizeLeft, maxSizeRight;

        maxSizeLeft = getLongestToString(curEntry.left, maxSize);

        maxSizeRight = getLongestToString(curEntry.right, maxSize);

        return Math.max(maxSize, Math.max(maxSizeLeft, maxSizeRight));
    }

    private String getSpace(char ch, int quant) {

        StringBuilder toReturn = new StringBuilder();

        while (quant-- > 0) {

            toReturn.append(ch);
        }

        return toReturn.toString();
    }

    private String getString(String data, int size) {

        StringBuilder toReturn = new StringBuilder();

        for (int i = 0; i < size - data.length(); i++) {

            toReturn.append(" ");
        }

        toReturn.append(data);

        return toReturn.toString();
    }

    public void checkBlackHeights(SimpleEntry top, int curHeight) {
        if (top == null) {
            return;
        }
        if (top.color) {
            curHeight++;
        }
        if (top.left != null) {
            checkBlackHeights(top.left, curHeight);
        }
        if (top.right != null) {
            checkBlackHeights(top.right, curHeight);
        }
        if (top.left == null && top.right == null) {
            System.out.println(top.key + ": " + curHeight);
        }
    }

    public static void main(String[] args) {
        MyTreeMap map = new MyTreeMap();
//        map.put(31, 31);
//        map.put(43, 43);
//        map.put(5, 5);
//        map.put(90, 90);
//        map.put(91, 91);
//        map.put(92, 92);
//        map.put(93, 93);
//        map.put(50, 50);
//        map.put(25, 25);
//        map.put(75, 75);
//        map.put(12, 12);
//        map.put(37, 37);
//        map.put(62, 62);
//        map.put(87, 87);
//        map.put(6, 6);
//        map.put(18, 18);
//        map.put(95, 95);
//        map.put(96, 96);
//        map.put(97, 97);
//        map.put(98, 98);
        for (int i = 0; i < 20; i++) {
            map.put((int) (Math.random() * 100), 0);
        }
//        for (int i = 0; i < 500; i++) {
//            map.put(i, 0);
//        }
//        Iterator<Entry> iterator = map.entryIterator();
//        while(iterator.hasNext()){
//            System.out.println(iterator.next());
//        }
        System.out.println(map);
        map.checkBlackHeights(map.root, 0);
    }
}