package de.timmi6790.discord_framework.datatypes;

import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Dequeue implementation that store a set of elements that was already added, abort add if element was already part of the queue
 *
 * @param <T> the type
 */
public class MemoryDeque<T> extends ArrayDeque<T> {
    private final Set<T> seenItemList = new HashSet<>();

    public MemoryDeque() {
        super();
    }

    public MemoryDeque(final int numElements) {
        super(numElements);
    }

    @Override
    public boolean add(@NonNull final T t) {
        if (!this.seenItemList.add(t)) {
            return false;
        }

        return super.add(t);
    }

    @Override
    public void clear() {
        this.seenItemList.clear();
        super.clear();
    }
}
