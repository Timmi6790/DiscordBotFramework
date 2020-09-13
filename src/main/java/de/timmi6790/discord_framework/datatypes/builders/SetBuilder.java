package de.timmi6790.discord_framework.datatypes.builders;

import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetBuilder<T> {
    private final Set<T> set;

    public static <T> SetBuilder<T> ofLinkedSet() {
        return new SetBuilder<>(LinkedHashSet::new);
    }

    public static <T> SetBuilder<T> ofHashSet() {
        return new SetBuilder<>(HashSet::new);
    }

    public static <T> SetBuilder<T> ofHashSet(final int size) {
        return new SetBuilder<>(() -> new HashSet<>(size));
    }

    public SetBuilder(@NonNull final Supplier<Set<T>> listFactory) {
        this.set = listFactory.get();
    }

    public SetBuilder<T> add(final T value, final boolean ifCondition) {
        if (ifCondition) {
            this.add(value);
        }
        return this;
    }

    public SetBuilder<T> add(final T value) {
        this.set.add(value);
        return this;
    }

    @SafeVarargs
    public final SetBuilder<T> addAll(@NonNull final Predicate<T> ifCondition, final T... values) {
        return this.addAll(ifCondition, Arrays.asList(values));
    }

    @SafeVarargs
    public final SetBuilder<T> addAll(final T... values) {
        this.addAll(Arrays.asList(values));
        return this;
    }

    public SetBuilder<T> addAll(@NonNull final Predicate<T> ifCondition, @NonNull final Stream<T> values) {
        return this.addAll(ifCondition, values.collect(Collectors.toList()));
    }

    public SetBuilder<T> addAll(@NonNull final Stream<T> values) {
        return this.addAll(values.collect(Collectors.toList()));
    }

    public SetBuilder<T> addAll(@NonNull final Predicate<T> ifCondition, @NonNull final Collection<T> values) {
        for (final T value : values) {
            this.add(value, ifCondition.test(value));
        }
        return this;
    }

    public SetBuilder<T> addAll(@NonNull final Collection<T> values) {
        for (final T value : values) {
            this.add(value);
        }
        return this;
    }

    public Set<T> build() {
        return this.set;
    }

    public Set<T> buildUnmodifiable() {
        return Collections.unmodifiableSet(this.build());
    }
}
