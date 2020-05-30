package de.timmi6790.statsbotdiscord.datatypes;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class MapBuilder<K, V> {
    private final Map<K, V> map;

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
