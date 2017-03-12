package dmu.project.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Dom on 27/01/2017.
 */

public class LIFOEntry<E extends Comparable<? super E>> implements Comparable<LIFOEntry<E>> {

    final static AtomicLong seq = new AtomicLong();
    final long seqNum;
    final E entry;

    public LIFOEntry(E entry) {
        this.entry = entry;
        seqNum = seq.getAndIncrement();
    }

    public E getEntry() {
        return entry;
    }

    public static void resetCount() {
        if (seq != null)
            seq.set(0);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if (o.getClass() == getClass()) {
            LIFOEntry other = (LIFOEntry) o;
            return entry.equals(other.entry);
        } else if (o.getClass() == entry.getClass()) {
            return entry.equals(o);
        }
        return false;
    }

    @Override
    public int compareTo(LIFOEntry<E> o) {
        int res = entry.compareTo(o.entry);
        if (res == 0)
            res = (seqNum < o.seqNum ? 1 : -1);
        return res;
    }
}