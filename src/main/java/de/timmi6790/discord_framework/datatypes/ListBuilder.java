package de.timmi6790.discord_framework.datatypes;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListBuilder<T> {
    private final List<T> list;
    
    public ListBuilder(final Supplier<List<T>> listFactory) {
        this.list = listFactory.get();
    }

    public ListBuilder<T> add(final T value) {
        this.list.add(value);
        return this;
    }

    public ListBuilder<T> addAll(final T... values) {
        this.list.addAll(java.util.Arrays.asList(values));
        return this;
    }

    public ListBuilder<T> addAll(final Stream<T> values) {
        return this.addAll(values.collect(Collectors.toList()));
    }

    public ListBuilder<T> addAll(final List<T> values) {
        this.list.addAll(values);
        return this;
    }

    public List<T> build() {
        return this.list;
    }

    public List<T> buildUnmodifiable() {
        return Collections.unmodifiableList(this.list);
    }
}
