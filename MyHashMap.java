package EPAM2015_lab9;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MyHashMap implements MyMap {

    private static class SimpleEntry implements MyHashMap.Entry {

        private final int hashCode;
        private final Object key;
        private Object value;
        private SimpleEntry next;

        public SimpleEntry(int hashCode, Object key, Object value) {
            this.key = key;
            this.value = value;
            this.hashCode = hashCode;
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
            if (o == this) {
                return true;
            }
            return o instanceof MyHashMap.SimpleEntry &&
                    Objects.equals(this.key, ((SimpleEntry) o).getKey()) &&
                    Objects.equals(this.value, ((SimpleEntry) o).getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    private class EntryIterator implements Iterator<MyMap.Entry> {

        private int count;
        private int curBasket = -1;
        private SimpleEntry curEntry;
        private int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public Entry next() {
            if (count >= size){
                throw new NoSuchElementException();
            }
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            curEntry = curEntry != null && curEntry.next != null ? curEntry.next : nextBasketHead();
            count++;
            return curEntry;
        }

        private SimpleEntry nextBasketHead() {
            for (int i = ++curBasket; i < table.length; i++) {
                if (table[i] != null) {
                    return table[curBasket = i];
                }
            }
            throw new NoSuchElementException();
        }
    }

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private SimpleEntry[] table;
    private float loadFactor;
    private int size;
    private int modCount;

    public MyHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.table = new SimpleEntry[DEFAULT_INITIAL_CAPACITY];
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: "
                    + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        } else {
            initialCapacity = tableSizeFor(initialCapacity);
        }
        table = new SimpleEntry[initialCapacity];
        this.loadFactor = loadFactor;
    }

    private static int tableSizeFor(int cap) {
        if (--cap > 0) {
            for (int mask = 1 << 30; mask > 0; mask >>>= 1) {
                if ((mask & cap) > 0) {
                    return mask >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : mask << 1;
                }
            }
        }
        return 1;
    }

    @Override
    public void clear() {
        this.table = new SimpleEntry[table.length];
        size = 0;
        modCount++;
    }

    @Override
    public boolean containsKey(Object key) {
        int hashCode = Objects.hashCode(key);
        int tableIndex = hashCode & table.length - 1;
        SimpleEntry curEntry = table[tableIndex];
        while (curEntry != null) {
            if (curEntry.key.equals(key)) {
                return true;
            }
            curEntry = curEntry.next;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        Iterator iter = entryIterator();
        while (iter.hasNext()) {
            if (((Entry) iter.next()).getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        int hashCode = Objects.hashCode(key);
        int tableIndex = hashCode & table.length - 1;
        SimpleEntry curEntry = table[tableIndex];
        while (curEntry != null) {
            if (curEntry.key.equals(key)) {
                return curEntry.value;
            }
            curEntry = curEntry.next;
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Object put(Object key, Object value) {
        int hashCode = Objects.hashCode(key);
        return put(hashCode, key, value);
    }

    private Object put(int hashCode, Object key, Object value) {
        int tableIndex = hashCode & table.length - 1;
        SimpleEntry toAdd = new SimpleEntry(hashCode, key, value);
        SimpleEntry curEntry = table[tableIndex];
        while (curEntry != null) {
            if (curEntry.key.equals(key)) {
                Object toReturn = curEntry.value;
                curEntry.value = value;
                return toReturn;
            }
            if (curEntry.next == null) {
                break;
            }
            curEntry = curEntry.next;
        }
        if (curEntry == null) {
            table[tableIndex] = toAdd;
        } else {
            curEntry.next = toAdd;
        }
        if (++size > table.length * loadFactor && table.length < MAXIMUM_CAPACITY) {
            resize();
        }
        modCount++;
        return null;
    }

    @Override
    public Object remove(Object key) {
        int hashCode = Objects.hashCode(key);
        int tableIndex = hashCode & table.length - 1;
        Object toReturn = null;
        SimpleEntry curEntry = table[tableIndex];
        if (curEntry == null) {
            return null;
        }
        if (curEntry.key.equals(key)) {
            toReturn = curEntry.value;
            table[tableIndex] = curEntry.next;
        } else {
            SimpleEntry prevEntry = curEntry;
            curEntry = curEntry.next;
            while (curEntry != null) {
                if (curEntry.key.equals(key)) {
                    toReturn = curEntry.value;
                    prevEntry.next = curEntry.next;
                    break;
                }
                prevEntry = curEntry;
                curEntry = curEntry.next;
            }
        }
        if (toReturn != null) {
            size--;
            modCount++;
        }
        return toReturn;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator entryIterator() {
        return new EntryIterator();
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder("{}");
        Iterator iter = entryIterator();
        while (iter.hasNext()) {
            toReturn.insert(toReturn.length() - 1, ", " + iter.next());
        }
        if (toReturn.length() > 2) {
            toReturn.delete(1, 3);
        }
        return toReturn.toString();
    }

    public void showMap() {
        StringBuilder sb = new StringBuilder("{}");
        StringBuilder sub;
        for (SimpleEntry curEntry : table) {
            sub = new StringBuilder("[" + curEntry + "]");
            while (curEntry != null && curEntry.next != null) {
                sub.insert(sub.length() - 1, ", " + (curEntry = curEntry.next));
            }
            sb.insert(sb.length() - 1, ", " + sub);
        }
        if (sb.length() > 2) {
            sb.delete(1, 3);
        }
        System.out.println(sb.toString());
    }

    private void resize() {
        int newCap = loadFactor == DEFAULT_LOAD_FACTOR
                ? this.table.length << 1 : tableSizeFor((int) Math.ceil(size / loadFactor));
        MyHashMap temp = new MyHashMap(newCap);
        Iterator iter = entryIterator();
        while (iter.hasNext()) {
            SimpleEntry curEntry = (SimpleEntry) iter.next();
            temp.put(curEntry.hashCode, curEntry.key, curEntry.value);
        }
        this.table = temp.table;
    }

    public static void main(String[] args) {
        MyHashMap map = new MyHashMap(1);
        System.out.println(map.size());
        System.out.println(map.table.length);
        map.put(4, 1);
        System.out.println(map);
        map.showMap();
        map.put(5, 1);
        System.out.println(map);
        map.showMap();
        map.put(6, 1);
        System.out.println(map);
        map.showMap();
        map.put(7, 1);
        System.out.println(map);
        map.showMap();
        System.out.println(map.size());
        System.out.println(map.table.length);
//        for (int i = 0; i < 100; i++) {
//            int curKey = (int) (Math.random() * 100);
//            map.put(curKey, curKey);
//            System.out.println("i = " + i);
//            System.out.println("size = " + map.size());
//            System.out.println(map);
//            map.showMap();
//        }
//        map.put(4, 1);
//        map.put(2, 2);
//        map.put(3, 3);
//        map.put(3, 31);
//        map.put(3, 32);
//        map.put(12, 12);
//        map.put(19, 19);
//        map.put(17, 17);
//        map.put(35, 35);
    }
}
