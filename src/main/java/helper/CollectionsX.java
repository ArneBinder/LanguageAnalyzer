package helper;

import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Arne on 05.09.2015.
 */
public final class CollectionsX {

    static class JoinedCollectionView<E> implements Collection<E> {

        private final Collection<? extends E>[] items;

        public JoinedCollectionView(final Collection<? extends E>[] items) {
            this.items = items;
        }

        public boolean addAll(final Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            for (final Collection<? extends E> coll : items) {
                coll.clear();
            }
        }

        public boolean contains(final Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(final Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            return !iterator().hasNext();
        }

        public Iterator<E> iterator() {
            return Iterables.concat(items).iterator();
        }

        public boolean remove(final Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(final Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(final Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            int ct = 0;
            for (final Collection<? extends E> coll : items) {
                ct += coll.size();
            }
            return ct;
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Returns a live aggregated collection view of the collections passed in.
     * <p>
     * All methods except {@link Collection#size()}, {@link Collection#clear()},
     * {@link Collection#isEmpty()} and {@link Iterable#iterator()}
     *  throw {@link UnsupportedOperationException} in the returned Collection.
     * <p>
     * None of the above methods is thread safe (nor would there be an easy way
     * of making them).
     */
    public static <T> Collection<T> combine(
            final Collection<? extends T>... items) {
        return new JoinedCollectionView<T>(items);
    }

    private CollectionsX() {
    }

}