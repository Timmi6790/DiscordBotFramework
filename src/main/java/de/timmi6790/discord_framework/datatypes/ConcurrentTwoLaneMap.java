package de.timmi6790.discord_framework.datatypes;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Concurrent map implementation based on 2 concurrent maps with O(1) for key and value operations.
 * Both keys and values need to be uniq.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class ConcurrentTwoLaneMap<K, V> extends AbstractMap<K, V> implements Serializable {
    private static final long serialVersionUID = 0;

    private final Map<K, V> keyToValueMap = new ConcurrentHashMap<>();
    private final Map<V, K> valueToKeyMap = new ConcurrentHashMap<>();

    @Override
    public int size() {
        return this.keyToValueMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean containsValue(final Object value) {
        return this.valueToKeyMap.containsKey(value);
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.keyToValueMap.containsKey(key);
    }

    @Override
    public V get(final Object key) {
        return this.keyToValueMap.get(key);
    }

    public V getValue(final K key) {
        return this.get(key);
    }

    public Optional<V> getValueOptional(final K key) {
        return Optional.ofNullable(this.getValue(key));
    }

    public K getKey(final V value) {
        return this.valueToKeyMap.get(value);
    }

    public Optional<K> getKeyOptional(final V value) {
        return Optional.ofNullable(this.getKey(value));
    }

    @Override
    public V put(final K key, final V value) {
        this.keyToValueMap.put(key, value);
        this.valueToKeyMap.put(value, key);

        return value;
    }

    @Override
    public V remove(final Object key) {
        final V value = this.keyToValueMap.remove(key);
        if (value != null) {
            this.valueToKeyMap.remove(value);
        }

        return value;
    }

    public V removeKey(final K key) {
        return this.remove(key);
    }

    public K removeValue(final V value) {
        final K key = this.valueToKeyMap.remove(value);
        if (key != null) {
            this.keyToValueMap.remove(key);
        }

        return key;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        this.keyToValueMap.clear();
        this.valueToKeyMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.keyToValueMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.valueToKeyMap.keySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.keyToValueMap.entrySet();
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return Optional.ofNullable(this.get(key)).orElse(defaultValue);
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        this.keyToValueMap.forEach(action);
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public V putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public V replace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(final K key, @NotNull final Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(final K key, @NotNull final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(final K key, @NotNull final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(final K key, @NotNull final V value, @NotNull final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }
}
