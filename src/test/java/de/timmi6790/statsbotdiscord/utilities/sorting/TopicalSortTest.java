package de.timmi6790.statsbotdiscord.utilities.sorting;

import de.timmi6790.statsbotdiscord.datatypes.ListBuilder;
import de.timmi6790.statsbotdiscord.exceptions.TopicalSortCycleException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TopicalSortTest {
    @Test
    void simplePathSort() throws TopicalSortCycleException {
        final List<Integer> values = Arrays.asList(10, 2, 5, 1, 4, 3, 8, 6, 7, 9);
        final List<TopicalSort.Dependency> dependencies = new ListBuilder<TopicalSort.Dependency>(ArrayList::new)
                .add(new TopicalSort.Dependency(0, 9))
                .add(new TopicalSort.Dependency(9, 6))
                .add(new TopicalSort.Dependency(6, 8))
                .add(new TopicalSort.Dependency(8, 7))
                .add(new TopicalSort.Dependency(7, 2))
                .add(new TopicalSort.Dependency(2, 4))
                .add(new TopicalSort.Dependency(4, 5))
                .add(new TopicalSort.Dependency(5, 1))
                .add(new TopicalSort.Dependency(1, 3))
                .build();

        final TopicalSort<Integer> topicalSort = new TopicalSort<>(values, dependencies);
        final List<Integer> sortedList = topicalSort.sort();

        final List<Integer> controlList = IntStream.range(1, 11)
                .boxed()
                .collect(Collectors.toList());
        assertThat(sortedList).isEqualTo(controlList);
    }

    @Test
    void simpleLoopDetectionCheck() {
        final List<Integer> values = Arrays.asList(1, 2);
        final List<TopicalSort.Dependency> dependencies = new ListBuilder<TopicalSort.Dependency>(ArrayList::new)
                .add(new TopicalSort.Dependency(0, 1))
                .add(new TopicalSort.Dependency(1, 0))
                .build();

        final TopicalSort<Integer> topicalSort = new TopicalSort<>(values, dependencies);
        assertThrows(TopicalSortCycleException.class, topicalSort::sort);
    }
}