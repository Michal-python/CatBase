package cat.michal.catbase.common.data;

import cat.michal.catbase.common.data.time.TimeSource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class TimedList<V> implements List<V> {

    public TimedList() {
        backingList = new ArrayList<>();
    }

    private final List<TimedRecord<V>> backingList;
    private TimeSource timeSource;
    private long timeout;

    public Object getMutex() {
        return mutex;
    }

    private final Object mutex = new Object();

    public TimeSource getTimeSource() {
        return timeSource;
    }

    public TimedList<V> setTimeSource(TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimedList<V> setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean isEmpty() {
        return backingList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        synchronized (mutex) {
            return backingList.stream().anyMatch(e -> Objects.equals(e.value, o));
        }
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return new TimedListIterator<>();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return null;
    }

    @Override
    public boolean add(V v) {
        synchronized (mutex) {
            return this.backingList.add(new TimedRecord<>(v, timeSource.nanos()));
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (mutex) {
            for (Iterator<TimedRecord<V>> it = backingList.iterator(); it.hasNext(); )
                if (Objects.equals(o, it.next().value)) {
                    it.remove();
                    return true;
                }
            return false;
        }
    }

    public boolean removeIf(Predicate<? super V> filter) {
        synchronized (mutex) {
            Objects.requireNonNull(filter);
            long now = timeSource.nanos();
            boolean removed = false;
            final Iterator<TimedRecord<V>> each = backingList.iterator();
            while (each.hasNext()) {
                var record = each.next();
                if (!record.isValid(timeout, now)) continue;
                if (filter.test(record.value)) {
                    each.remove();
                    removed = true;
                }
            }
            return removed;
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        synchronized (mutex) {
            return c.stream().allMatch(this::contains);
        }
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends V> c) {
        synchronized (mutex) {
            c.forEach(this::add);
            return !c.isEmpty();
        }
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends V> c) {
        synchronized (mutex) {
            long now = timeSource.nanos();
            return backingList.addAll(index, c.stream().map(v -> new TimedRecord<V>(v, now)).toList());
        }
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        synchronized (mutex) {
            return backingList.removeIf(record -> c.contains(record.value));
        }
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        synchronized (mutex) {
            return backingList.removeIf(record -> !c.contains(record.value));
        }
    }

    @Override
    public void clear() {
        synchronized (mutex) {
            backingList.clear();
        }
    }

    @Override
    public V get(int index) {
        synchronized (mutex) {
            var record = backingList.get(index);
            if (record.isValid(timeout, timeSource.nanos()))
                return record.value;
            backingList.remove(index);
            return null;
        }
    }

    @Override
    public V set(int index, V element) {
        synchronized (mutex) {
            var removed = backingList.set(index, new TimedRecord<>(element, timeSource.nanos()));
            return removed != null ? removed.value : null;
        }
    }

    @Override
    public void add(int index, V element) {
        synchronized (mutex) {
            backingList.add(index, new TimedRecord<>(element, timeSource.nanos()));
        }
    }

    @Override
    public V remove(int index) {
        synchronized (mutex) {
            var removed = backingList.remove(index);
            return removed != null ? removed.value : null;
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (mutex) {
            long now = timeSource.nanos();
            for (int i = 0; i < backingList.size(); i++) {
                if (backingList.get(i).isValid(timeout, now) && Objects.equals(o, backingList.get(i).value)) {
                    return i;
                }
            }
            return -1;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (mutex) {
            long now = timeSource.nanos();
            for (int i = backingList.size() - 1; i >= 0; i--) {
                if (backingList.get(i).isValid(timeout, now) && Objects.equals(o, backingList.get(i).value)) {
                    return i;
                }
            }
            return -1;
        }
    }

    @NotNull
    @Override
    public ListIterator<V> listIterator() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NotNull
    @Override
    public ListIterator<V> listIterator(int index) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NotNull
    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Finds the first element matching the provided predicate and removes it from the list.
     * Timed out elements will not be returned.
     *
     * @param filter The predicate
     * @return the element or null
     */
    public V findAndRemove(Predicate<? super V> filter) {
        synchronized (mutex) {
            Objects.requireNonNull(filter);
            long now = timeSource.nanos();
            for (int i = 0; i < backingList.size(); i++) {
                var record = backingList.get(i);
                if (!record.isValid(timeout, now)) continue;
                if (filter.test(record.value)) {
                    backingList.remove(i);
                    return record.value;
                }
            }
            return null;
        }
    }

    public void removeStale() {
        synchronized (mutex) {
            long now = timeSource.nanos();
            this.backingList.removeIf(e -> !e.isValid(timeout, now));
        }
    }

    private record TimedRecord<V>(
        V value,
        long time
    ) {
        boolean isValid(long timeout, long now) {
            return this.time + timeout > now;
        }
    }

    private class TimedListIterator<T> implements Iterator<T> {
        private int currentIndex = 0;
        private boolean canRemove = false;

        @Override
        public boolean hasNext() {
            return currentIndex < backingList.size();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            canRemove = true;
            return (T) backingList.get(currentIndex++).value;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            currentIndex--;
            backingList.remove(currentIndex);
            canRemove = false;
        }
    }
}
