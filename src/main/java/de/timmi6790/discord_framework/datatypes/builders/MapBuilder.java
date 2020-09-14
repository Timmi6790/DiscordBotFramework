package de.timmi6790.discord_framework.datatypes.builders;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class MapBuilder<K, V> {
    private final Map<K, V> map;

    public static <K, V> MapBuilder<K, V> ofHashMap() {
        return new MapBuilder<>(HashMap::new);
    }

    public static <K, V> MapBuilder<K, V> ofHashMap(final int size) {
        return new MapBuilder<>(() -> new HashMap<>(size));
    }

    public MapBuilder(@NonNull final Supplier<Map<K, V>> mapFactory) {
        this.map = mapFactory.get();
    }

    public MapBuilder<K, V> put(@Nullable final K key, @Nullable final V value) {
        this.map.put(key, value);
        return this;
    }

    public MapBuilder<K, V> put(@Nullable final K key, @Nullable final V value, final boolean ifCondition) {
        if (ifCondition) {
            this.map.put(key, value);
        }
        return this;
    }

    public MapBuilder<K, V> putAll(@NonNull final Map<K, V> map) {
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public MapBuilder<K, V> putAll(@NonNull final Map<K, V> map, @NonNull final BiPredicate<K, V> ifCondition) {
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue(), ifCondition.test(entry.getKey(), entry.getValue()));
        }
        return this;
    }

    public Map<K, V> build() {
        return this.map;
    }

    public Map<K, V> buildUnmodifiable() {
        return Collections.unmodifiableMap(this.map);
    }
}
