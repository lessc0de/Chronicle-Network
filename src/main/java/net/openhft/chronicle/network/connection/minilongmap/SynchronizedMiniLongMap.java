package net.openhft.chronicle.network.connection.minilongmap;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**\
 * A synchronized minimap with primitive long keys and values of any type V.
 *
 * @param <V> value type
 */
final class SynchronizedMiniLongMap<V> implements MiniLongMap<V> {

    private final MiniLongMap<V> delegate;

    SynchronizedMiniLongMap(@NotNull MiniLongMap<V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized V put(long key, @NotNull V value) {
        return delegate.put(key, value);
    }

    @Override
    public synchronized V get(long key) {
        return delegate.get(key);
    }

    @Override
    public synchronized V remove(long key) {
        return delegate.remove(key);
    }

    @Override
    public synchronized void removeIf(@NotNull LongObjPredicate<? super V> filter) {
        delegate.removeIf(filter);
    }

    @Override
    public synchronized void clear() {
        delegate.clear();
    }

    @Override
    public synchronized void forEachValue(@NotNull Consumer<? super V> action) {
        delegate.forEachValue(action);
    }

    @Override
    public synchronized void forEachKey(@NotNull LongConsumer action) {
        delegate.forEachKey(action);
    }
}