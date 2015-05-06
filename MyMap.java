package EPAM2015_lab9;

import java.util.Iterator;

public interface MyMap {

    interface Entry {
        boolean equals(Object o);

        Object getKey();

        Object getValue();

        int hashCode();

        Object setValue(Object value);
    }

    void clear();

    boolean containsKey(Object key);

    boolean containsValue(Object value);

    Object get(Object key);

    boolean isEmpty();

    Object put(Object key, Object value);

    Object remove(Object key);

    int size();

    Iterator entryIterator();
}
