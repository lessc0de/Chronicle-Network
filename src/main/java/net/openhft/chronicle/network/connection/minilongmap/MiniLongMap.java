package net.openhft.chronicle.network.connection.minilongmap;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**\
 * A minimap with primitive long keys and values of any type V.
 *
 * @param <V> value type
 */
public interface MiniLongMap<V> {

    /**
     * Associates the specified value with the specified key in this minimap.
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>,
     *         if the implementation supports <tt>null</tt> values.)
     * @throws NullPointerException if the specified value is null
     */
    V put(long key, @NotNull V value);

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this minimap contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>If this minimap does not permit null values
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    V get(long key);

    /**
     * Removes the mapping for a key from this minimap if it is present.
     * More formally, if this map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     *
     * <p>Returns the value to which this map previously associated the key,
     * or <tt>null</tt> if the map contained no mapping for the key.
     *
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    V remove(long key);

    /**
     * Removes all of the elements of this minimap that satisfy the given
     * predicate.  Errors or runtime exceptions thrown during iteration or by
     * the predicate are relayed to the caller.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     *        removed
     * @throws NullPointerException if the specified filter is null
     */
    void removeIf(@NotNull LongObjPredicate<? super V> filter);

    /**
     * Removes all of the mappings from this minimap.
     * The map will be empty after this call returns.
     */
    void clear();

    /**
     * Performs the given action for each value in this minimap until all values
     * have been processed or the action throws an exception. The order in which
     * values are processed is unspecified.
     * Exceptions thrown by the action are relayed to the caller.
     *
     * The action may not modify the minimap.
     *
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     */
    void forEachValue(@NotNull Consumer<? super V> action);

    /**
     * Performs the given action for each key this minimap until all keys
     * have been processed or the action throws an exception. The order in which
     * values are processed is unspecified.
     * Exceptions thrown by the action are relayed to the caller.
     *
     * The action may not modify the minimap.
     *
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     */
    void forEachKey(@NotNull LongConsumer action);

    @FunctionalInterface
    interface LongObjPredicate<T> {
        boolean test(long key, T value);
    }

    /**
     * Creates and returns a new MiniLongMap with at least the
     * provided {@code minSize} of primitive buckets. Overflows are placed
     * in a backing collection that might be slower and might use wrapping objects.
     * <p>
     * The returned MiniLongMap is <em>not thread safe</em>.
     *
     * @param minSize of primitive buckets
     * @param <V> value type
     * @return a new MiniLongMap with at least the
     *         provided {@code minSize} of primitive buckets
     */
    static <V> MiniLongMap<V> create(final int minSize) {
        return new VanillaMiniLongMap<>(minSize);
    }

    /**
     * Creates and returns a new synchronized MiniLongMap with at least the
     * provided {@code minSize} of primitive buckets. Overflows are placed
     * in a backing collection that might be slower and might use wrapping objects.
     * <p>
     * The returned MiniLongMap is thread safe.
     *
     * @param minSize of primitive buckets
     * @param <V> value type
     * @return a new MiniLongMap with at least the
     *         provided {@code minSize} of primitive buckets
     */
    static <V> MiniLongMap<V> createSynchronized(final int minSize) {
        return new SynchronizedMiniLongMap<>(create(minSize));
    }

}