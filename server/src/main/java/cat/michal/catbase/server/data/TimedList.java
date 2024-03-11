package cat.michal.catbase.server.data;

import cat.michal.catbase.server.data.time.TimeSource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TimedList<V> implements List<V> {

    public TimedList() {
        backingList = new ArrayList<>();
    }

    private final List<TimedRecord<V>> backingList;
    private TimeSource timeSource;
    private long timeout;

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
        return backingList.stream().anyMatch(e -> Objects.equals(e.value, o));
    }

    /**
     * Warn, this iterator probably won't allow removal
     */
    @NotNull
    @Override
    public Iterator<V> iterator() {
        return valueStream().iterator();
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
        return this.backingList.add(new TimedRecord<>(v, timeSource.nanos()));
    }

    @Override
    public boolean remove(Object o) {
        for (Iterator<TimedRecord<V>> it = backingList.iterator(); it.hasNext(); )
            if (Objects.equals(o, it.next().value)) {
                it.remove();
                return true;
            }
        return false;
    }

    public boolean removeIf(Predicate<? super V> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<TimedRecord<V>> each = backingList.iterator();
        while (each.hasNext()) {
            if (filter.test(each.next().value)) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends V> c) {
        c.forEach(this::add);
        return !c.isEmpty();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends V> c) {
        long now = timeSource.nanos();
        return backingList.addAll(index, c.stream().map(v -> new TimedRecord<V>(v, now)).toList());
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return backingList.removeIf(record -> c.contains(record.value));
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return backingList.removeIf(record -> !c.contains(record.value));
    }

    @Override
    public void clear() {
        backingList.clear();
    }

    @Override
    public V get(int index) {
        var record = backingList.get(index);
        if (record.isValid(timeout, timeSource.nanos()))
            return record.value;
        backingList.remove(index);
        return null;
    }

    @Override
    public V set(int index, V element) {
        var removed = backingList.set(index, new TimedRecord<>(element, timeSource.nanos()));
        return removed != null ? removed.value : null;
    }

    @Override
    public void add(int index, V element) {
        backingList.add(index, new TimedRecord<>(element, timeSource.nanos()));
    }

    @Override
    public V remove(int index) {
        var removed = backingList.remove(index);
        return removed != null ? removed.value : null;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < backingList.size(); i++) {
            if (Objects.equals(o, backingList.get(i).value)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = backingList.size() - 1; i >= 0; i--) {
            if (Objects.equals(o, backingList.get(i).value)) {
                return i;
            }
        }
        return -1;
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

    private Stream<V> valueStream() {
        long now = timeSource.nanos();
        return backingList.stream().filter(vTimedRecord -> vTimedRecord.isValid(timeout, now)).map(i -> i.value);
    }

    public void removeStale() {
        long now = timeSource.nanos();
        this.backingList.removeIf(e -> !e.isValid(timeout, now));
    }

    private record TimedRecord<V>(
        V value,
        long time
    ) {
        boolean isValid(long timeout, long now) {
            return this.time + timeout > now;
        }
    }
}
