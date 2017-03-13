package dmu.project.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Dom on 27/01/2017.
 * <p>
 * Wrapper class to enable Last in First Out behaviour to break ties in priority queue.
 * Class contains a static atomic long that is incremented every time the object is constructed.
 * This is used to timestamp the entries.
 */

public class LIFOEntry<E extends Comparable<? super E>> implements Comparable<LIFOEntry<E>> {

    final static AtomicLong seq = new AtomicLong();
    final long seqNum;
    final E entry;

    /**
     * Constructor.
     *
     * @param entry The object to wrap.
     */
    public LIFOEntry(E entry) {
        this.entry = entry;
        seqNum = seq.getAndIncrement();
    }

    /**
     * @return The wrapped object.
     */
    public E getEntry() {
        return entry;
    }

    /**
     * Resets the static atomic long.
     */
    public static void resetCount() {
        if (seq != null)
            seq.set(0);
    }

    /**
     * Compares the wrapped object with the specified object.
     *
     * @param o The object to compare to.
     * @return Returns the wrapped object's equals result.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o.getClass() == getClass()) {
            LIFOEntry other = (LIFOEntry) o;
            return entry.equals(other.entry);
        } else if (o.getClass() == entry.getClass()) {
            return entry.equals(o);
        }
        return false;
    }

    /**
     * Comparison method to compare LIFOEntries and their wrapped object.
     * If the wrapped objects aren't equal it returns their comparison result.
     * If they are equal use the sequence number to break the tie.
     *
     * @param o The LIFOEntry to compare to.
     * @return 1 if this is greater than the specified object.
     */
    @Override
    public int compareTo(LIFOEntry<E> o) {
        int res = entry.compareTo(o.entry);
        if (res == 0)
            res = (seqNum < o.seqNum ? 1 : -1);
        return res;
    }
}