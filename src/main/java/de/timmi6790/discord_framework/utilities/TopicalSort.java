package de.timmi6790.discord_framework.utilities;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
public class TopicalSort<T> {
    private final List<T> vertices;
    private final boolean[][] adjacency;

    public TopicalSort(final List<T> vertices, final List<Dependency> dependencies) {
        this.vertices = vertices;
        this.adjacency = new boolean[this.vertices.size()][this.vertices.size()];

        log.debug("Dependencies: {}", dependencies);
        for (final Dependency edge : dependencies) {
            this.adjacency[edge.getId()][edge.getDependencyId()] = true;
        }
    }

    public List<T> sort() throws TopicalSortCycleException {
        final List<T> result = new ArrayList<>();
        final Queue<Integer> queue = IntStream.range(0, this.vertices.size())
                .boxed()
                .collect(Collectors.toCollection(ArrayDeque::new));

        boolean cycle;
        while (!queue.isEmpty()) {
            cycle = true;
            final Iterator<Integer> queueIterator = queue.iterator();
            while (queueIterator.hasNext()) {
                final int element = queueIterator.next();
                if (!this.hasDependency(element, queue)) {
                    queueIterator.remove();
                    result.add(this.vertices.get(element));
                    cycle = false;
                    break;
                }
            }

            if (cycle) {
                throw new TopicalSortCycleException();
            }
        }

        return result;
    }

    private boolean hasDependency(final int mainIndex, @NonNull final Queue<Integer> queue) {
        for (final int index : queue) {
            if (this.adjacency[mainIndex][index]) {
                return true;
            }
        }

        return false;
    }

    @Data
    public static class Dependency {
        private final int id;
        private final int dependencyId;
    }
}
