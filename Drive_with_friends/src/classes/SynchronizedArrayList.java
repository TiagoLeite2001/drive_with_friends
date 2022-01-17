package classes;

import java.util.ArrayList;

public class SynchronizedArrayList<T> {
    private ArrayList<T> list = new ArrayList();

    public synchronized void add(T o) {
        list.add(o);
    }

    public synchronized ArrayList getAll() {
        return list;
    }
}
