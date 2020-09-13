package de.timmi6790.discord_framework.datatypes.builders;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListBuilderTest {
    private static final String[] TEST_VALUES = new String[]{"1", "Test", "Test 12", null, "d"};

    @Test
    void ofLinkedList() {
        final List<String> isList = ListBuilder.<String>ofLinkedList().build();
        assertThat(isList).isInstanceOf(LinkedList.class);
    }

    @Test
    void ofArrayList() {
        final List<String> isList = ListBuilder.<String>ofArrayList().build();
        assertThat(isList).isInstanceOf(ArrayList.class);
    }

    @Test
    void ofArrayListSize() {
        final List<String> isList = ListBuilder.<String>ofArrayList(2).build();
        assertThat(isList).isInstanceOf(ArrayList.class);
    }

    @Test
    void add() {
        final ListBuilder<String> listBuilder = ListBuilder.ofArrayList(2);
        for (final String value : TEST_VALUES) {
            listBuilder.add(value);
        }

        final List<String> isList = listBuilder.build();
        assertThat(isList).hasSize(TEST_VALUES.length).containsAll(Arrays.asList(TEST_VALUES));
    }

    @Test
    void addIfCondition() {
        for (int index = 0; TEST_VALUES.length > index; index++) {
            final String value = TEST_VALUES[index];
            final boolean condition = index % 2 == 0;

            final List<String> isList = ListBuilder.<String>ofArrayList()
                    .add(value, condition)
                    .build();

            if (condition) {
                assertThat(isList)
                        .hasSize(1)
                        .contains(value);
            } else {
                assertThat(isList)
                        .isEmpty();
            }
        }
    }

    @Test
    void addAllArray() {
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(TEST_VALUES)
                .build();
        assertThat(isList)
                .hasSize(TEST_VALUES.length)
                .contains(TEST_VALUES);
    }

    @Test
    void addAllArrayIfCondition() {
        final List<String> addedValues = new ArrayList<>();
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(s -> {
                    final boolean addValue = ThreadLocalRandom.current().nextBoolean();
                    if (addValue) {
                        addedValues.add(s);
                    }
                    return addValue;
                }, TEST_VALUES)
                .build();

        assertThat(isList)
                .hasSize(addedValues.size())
                .containsAll(addedValues);
    }

    @Test
    void addAllStream() {
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(Arrays.stream(TEST_VALUES))
                .build();
        assertThat(isList)
                .hasSize(TEST_VALUES.length)
                .contains(TEST_VALUES);
    }

    @Test
    void addAllStreamIfCondition() {
        final List<String> addedValues = new ArrayList<>();
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(s -> {
                    final boolean addValue = ThreadLocalRandom.current().nextBoolean();
                    if (addValue) {
                        addedValues.add(s);
                    }
                    return addValue;
                }, Arrays.stream(TEST_VALUES))
                .build();

        assertThat(isList)
                .hasSize(addedValues.size())
                .containsAll(addedValues);
    }

    @Test
    void addAllCollection() {
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(Arrays.asList(TEST_VALUES))
                .build();
        assertThat(isList)
                .hasSize(TEST_VALUES.length)
                .contains(TEST_VALUES);
    }

    @Test
    void addAllCollectionIfCondition() {
        final List<String> addedValues = new ArrayList<>();
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(s -> {
                    final boolean addValue = ThreadLocalRandom.current().nextBoolean();
                    if (addValue) {
                        addedValues.add(s);
                    }
                    return addValue;
                }, Arrays.asList(TEST_VALUES))
                .build();

        assertThat(isList)
                .hasSize(addedValues.size())
                .containsAll(addedValues);
    }

    @Test
    void build() {
        final List<String> otherTestValues = Arrays.asList("DDDDDD", "asdsdasdasdas", "DSSDSDDSASAD", "DSDDSSDDSSD");
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(Arrays.asList(TEST_VALUES))
                .build();
        isList.addAll(otherTestValues);

        assertThat(isList)
                .hasSize(TEST_VALUES.length + otherTestValues.size())
                .contains(TEST_VALUES)
                .containsAll(otherTestValues);
    }

    @Test
    void buildUnmodifiable() {
        final List<String> isList = ListBuilder.<String>ofArrayList()
                .addAll(Arrays.asList(TEST_VALUES))
                .buildUnmodifiable();
        assertThrows(UnsupportedOperationException.class, () -> isList.add("TesT"));
    }
}