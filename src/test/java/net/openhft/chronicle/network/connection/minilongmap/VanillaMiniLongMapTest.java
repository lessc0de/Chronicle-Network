package net.openhft.chronicle.network.connection.minilongmap;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public final class VanillaMiniLongMapTest {

    private static final int MIN_SIZE = 17;
    private VanillaMiniLongMap<Integer> instance;
    private Map<Long, Integer> reference;

    @Before
    public void setup() {
        instance = new VanillaMiniLongMap<>(MIN_SIZE);
        reference = new HashMap<>();
    }

    @Test
    public void put() {
        final Integer a = instance.put(1, 1);
        assertNull(a);
        final Integer b = instance.put(1, 1);
        assertEquals((Integer) 1, b);
    }

    @Test
    public void get() {
        instance.put(1, 1);
        final Integer actual = instance.get(1);
        assertEquals((Integer) 1, actual);
    }

    @Test
    public void remove() {
        putMany();
        put(1, 1);
        final Integer actual = instance.remove(1);
        assertEquals((Integer) 1, actual);
        assertNull(instance.remove(1));

        final Set<Long> keys = new HashSet<>();
        instance.forEachKey(keys::add);

        keys.forEach(k -> {
            final Integer expected = instance.get(k);
            assertEquals(expected, instance.remove(k));
            assertNull(instance.remove(k));
        });
    }

    @Test
    public void removeIf() {
        putMany();
        put(1, 1);
        instance.removeIf((k, v) -> k == 1 && v.equals(1));
        assertNull(instance.get(1));
    }

    @Test
    public void clear() {
        putMany();
        instance.clear();
        instance.forEachValue(v -> fail("Map is not empty"));
    }

    @Test
    public void forEachValue() {
        putMany();
        final Set<Integer> expected = new HashSet<>(reference.values());
        final Set<Integer> actual = new HashSet<>();
        instance.forEachValue(actual::add);
        assertEquals(expected, actual);
    }

    @Test
    public void forEachKey() {
        putMany();
        final Set<Long> expected = reference.keySet();
        final Set<Long> actual = new HashSet<>();
        instance.forEachKey(actual::add);
        assertEquals(expected, actual);
    }

    private void putMany() {
        final Random random = new Random(42);
        for (int i = 0; i < MIN_SIZE * 3; i++) {
            put(random.nextLong(), random.nextInt());
        }
    }

    private void put(long key, Integer value) {
        instance.put(key, value);
        reference.put(key, value);
    }

}