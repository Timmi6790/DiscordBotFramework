package de.timmi6790.discord_framework.datatypes.builders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MapBuilder<K, V> {
    private final Map<K, V> map;

    public static <K, V> MapBuilder<K, V> ofHashMap(final int size) {
        return new MapBuilder<>(() -> new HashMap<>(size));
    }

    public MapBuilder(final Supplier<Map<K, V>> mapFactory) {
        this.map = mapFactory.get();
    }

    public MapBuilder<K, V> put(final K key, final V value) {
        this.map.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return this.map;
    }

    public Map<K, V> buildUnmodifiable() {
        return Collections.unmodifiableMap(this.map);
    }
}
