package net.openhft.chronicle.network.connection.minilongmap;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

final class VanillaMiniLongMap<V> implements MiniLongMap<V> {

    // Must be a power of 2
    private final int size;
    private final long mask;

    private final long[] keys;
    private final V[] values;

    private final Map<Long, V> hashDuplicates;

    VanillaMiniLongMap(final int minSize) {
        size = minSize < 16
                ? 16
                :  Integer.highestOneBit(minSize) * 2;

        mask = size - 1;
        keys = new long[size];
        values = (V[]) new Object[size];
        hashDuplicates = new HashMap<>();
    }

    @Override
    public V put(long key, @NotNull V value) {
        final int index = miniHash(key);
        if (values[index] == null) {
            // Empty slot
            keys[index] = key;
            values[index] = value;
            // Depending on history, there might be
            // an old entry in the hashDuplicates
            if (hashDuplicates.isEmpty())
                return null;
            else
                return hashDuplicates.remove(key);
        } else if (keys[index] == key) {
            // Overwrite value for existing key
            final V previous = values[index];
            values[index] = value;
            return previous;
        } else {
            // Collision
            return hashDuplicates.put(key, value);
        }
    }

    @Override
    public V get(long key) {
        final int index = miniHash(key);
        final V value = values[index];
        if (keys[index] == key && value != null) {
            return value;
        }
        if (hashDuplicates.isEmpty())
            return null;
        else
            return hashDuplicates.get(key);
    }

    @Override
    public V remove(long key) {
        // Depending on history, an entry might be
        // in either direct arrays or in the hashDuplicates
        final int index = miniHash(key);
        final V value = values[index];
        if (keys[index] == key && value != null) {
            // Hit in the arrays
            values[index] = null;
            return value;
        }
        if (hashDuplicates.isEmpty())
            return null;
        else
            return hashDuplicates.remove(key);
    }

    @Override
    public void removeIf(@NotNull LongObjPredicate<? super V> filter) {
        for (int i = 0; i < size; i++) {
            final V value = values[i];
            if (value != null)
                if (filter.test(keys[i], value))
                    values[i] = null;
        }
        // This can be improved
        final List<Long> toRemove = new ArrayList<>();
        hashDuplicates.forEach((k, v) -> {
            if (filter.test(k, v)) {
                toRemove.add(k);
            }
        });
        toRemove.forEach(hashDuplicates::remove);
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            values[i] = null;
        }
        hashDuplicates.clear();
    }

    @Override
    public void forEachValue(@NotNull Consumer<? super V> action) {
        for (int i = 0; i < size; i++) {
            final V value = values[i];
            if (value != null)
                action.accept(value);
        }
        hashDuplicates.values().forEach(action);
    }

    @Override
    public void forEachKey(@NotNull LongConsumer action) {
        for (int i = 0; i < size; i++) {
            final V value = values[i];
            if (value != null)
                action.accept(keys[i]);
        }
        hashDuplicates.keySet().forEach(action::accept);
    }

    private int miniHash(long key) {
        return (int) (key & mask);
    }

}