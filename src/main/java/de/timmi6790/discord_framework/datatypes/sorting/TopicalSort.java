package de.timmi6790.discord_framework.datatypes.sorting;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TopicalSort<T> {
    private final List<T> vertices;
    private final boolean[][] adjacency;

    public TopicalSort(final List<T> vertices, final List<Dependency> dependencies) {
        this.vertices = vertices;
        this.adjacency = new boolean[this.vertices.size()][this.vertices.size()];

        DiscordBot.getLogger().debug("Dependencies: " + dependencies);
        dependencies.forEach(edge -> this.adjacency[edge.getId()][edge.getDependencyId()] = true);
    }

    public List<T> sort() throws TopicalSortCycleException {
        final List<T> result = new ArrayList<>();
        final Queue<Integer> queue = IntStream.range(0, this.vertices.size())
                .boxed()
                .collect(Collectors.toCollection(ArrayDeque::new));

        boolean cycle;
        while (!queue.isEmpty()) {
            cycle = true;
            for (final int element : queue) {
                if (!this.hasDependency(element, queue)) {
                    queue.remove(element);
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
        return queue.stream()
                .map(index -> this.adjacency[mainIndex][index])
                .filter(bool -> bool)
                .findAny()
                .orElse(false);
    }

    @Data
    public static class Dependency {
        private final int id;
        private final int dependencyId;
    }
}
