package de.timmi6790.discord_framework.datatypes.builders;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListBuilder<T> {
    private final List<T> list;

    public static <T> ListBuilder<T> ofLinkedList() {
        return new ListBuilder<>(LinkedList::new);
    }

    public static <T> ListBuilder<T> ofArrayList() {
        return new ListBuilder<>(ArrayList::new);
    }

    public static <T> ListBuilder<T> ofArrayList(final int size) {
        return new ListBuilder<>(() -> new ArrayList<>(size));
    }

    public ListBuilder(@NonNull final Supplier<List<T>> listFactory) {
        this.list = listFactory.get();
    }

    public ListBuilder<T> add(@Nullable final T value, final boolean ifCondition) {
        if (ifCondition) {
            this.add(value);
        }
        return this;
    }

    public ListBuilder<T> add(final T value) {
        this.list.add(value);
        return this;
    }

    @SafeVarargs
    public final ListBuilder<T> addAll(@NonNull final Predicate<T> ifCondition, @Nullable final T... values) {
        return this.addAll(ifCondition, Arrays.asList(values));
    }

    @SafeVarargs
    public final ListBuilder<T> addAll(@Nullable final T... values) {
        this.addAll(Arrays.asList(values));
        return this;
    }

    public ListBuilder<T> addAll(@NonNull final Predicate<T> ifCondition, @NonNull final Stream<T> values) {
        return this.addAll(ifCondition, values.collect(Collectors.toList()));
    }

    public ListBuilder<T> addAll(@NonNull final Stream<T> values) {
        return this.addAll(values.collect(Collectors.toList()));
    }

    public ListBuilder<T> addAll(@NonNull final Predicate<T> ifCondition, @NonNull final Collection<T> values) {
        for (final T value : values) {
            this.add(value, ifCondition.test(value));
        }
        return this;
    }

    public ListBuilder<T> addAll(@NonNull final Collection<T> values) {
        for (final T value : values) {
            this.add(value);
        }
        return this;
    }

    public List<T> build() {
        return this.list;
    }

    public List<T> buildUnmodifiable() {
        return Collections.unmodifiableList(this.build());
    }
}
