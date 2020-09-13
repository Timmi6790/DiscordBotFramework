package de.timmi6790.discord_framework.datatypes.builders;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SetBuilderTest {
    private static final String[] TEST_VALUES = new String[]{"1", "Test", "Test 12", null, "d"};

    @Test
    void ofLinkedSet() {
        final Set<String> isSet = SetBuilder.<String>ofLinkedSet().build();
        assertThat(isSet).isInstanceOf(LinkedHashSet.class);
    }

    @Test
    void ofHashSet() {
        final Set<String> isSet = SetBuilder.<String>ofHashSet().build();
        assertThat(isSet).isInstanceOf(HashSet.class);
    }

    @Test
    void ofHashSetSize() {
        final Set<String> isSet = SetBuilder.<String>ofHashSet(2).build();
        assertThat(isSet).isInstanceOf(HashSet.class);
    }

    @Test
    void add() {
        final SetBuilder<String> listBuilder = SetBuilder.ofHashSet();
        for (final String value : TEST_VALUES) {
            listBuilder.add(value);
        }

        final Set<String> isSet = listBuilder.build();
        assertThat(isSet).hasSize(TEST_VALUES.length).containsAll(Arrays.asList(TEST_VALUES));
    }

    @Test
    void addIfCondition() {
        for (int index = 0; TEST_VALUES.length > index; index++) {
            final String value = TEST_VALUES[index];
            final boolean condition = index % 2 == 0;

            final Set<String> isSet = SetBuilder.<String>ofHashSet()
                    .add(value, condition)
                    .build();

            if (condition) {
                assertThat(isSet)
                        .hasSize(1)
                        .contains(value);
            } else {
                assertThat(isSet)
                        .isEmpty();
            }
        }
    }

    @Test
    void addAllArray() {
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(TEST_VALUES)
                .build();
        assertThat(isSet)
                .hasSize(TEST_VALUES.length)
                .contains(TEST_VALUES);
    }

    @Test
    void addAllArrayIfCondition() {
        final List<String> addedValues = new ArrayList<>();
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(s -> {
                    final boolean addValue = ThreadLocalRandom.current().nextBoolean();
                    if (addValue) {
                        addedValues.add(s);
                    }
                    return addValue;
                }, TEST_VALUES)
                .build();

        assertThat(isSet)
                .hasSize(addedValues.size())
                .containsAll(addedValues);
    }

    @Test
    void addAllStream() {
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(Arrays.stream(TEST_VALUES))
                .build();
        assertThat(isSet)
                .hasSize(TEST_VALUES.length)
                .contains(TEST_VALUES);
    }

    @Test
    void addAllStreamIfCondition() {
        final List<String> addedValues = new ArrayList<>();
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(s -> {
                    final boolean addValue = ThreadLocalRandom.current().nextBoolean();
                    if (addValue) {
                        addedValues.add(s);
                    }
                    return addValue;
                }, Arrays.stream(TEST_VALUES))
                .build();

        assertThat(isSet)
                .hasSize(addedValues.size())
                .containsAll(addedValues);
    }

    @Test
    void addAllCollection() {
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(Arrays.asList(TEST_VALUES))
                .build();
        assertThat(isSet)
                .hasSize(TEST_VALUES.length)
                .contains(TEST_VALUES);
    }

    @Test
    void addAllCollectionIfCondition() {
        final List<String> addedValues = new ArrayList<>();
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(s -> {
                    final boolean addValue = ThreadLocalRandom.current().nextBoolean();
                    if (addValue) {
                        addedValues.add(s);
                    }
                    return addValue;
                }, Arrays.asList(TEST_VALUES))
                .build();

        assertThat(isSet)
                .hasSize(addedValues.size())
                .containsAll(addedValues);
    }

    @Test
    void build() {
        final List<String> otherTestValues = Arrays.asList("DDDDDD", "asdsdasdasdas", "DSSDSDDSASAD", "DSDDSSDDSSD");
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(Arrays.asList(TEST_VALUES))
                .build();
        isSet.addAll(otherTestValues);

        assertThat(isSet)
                .hasSize(TEST_VALUES.length + otherTestValues.size())
                .contains(TEST_VALUES)
                .containsAll(otherTestValues);
    }

    @Test
    void buildUnmodifiable() {
        final Set<String> isSet = SetBuilder.<String>ofHashSet()
                .addAll(Arrays.asList(TEST_VALUES))
                .buildUnmodifiable();
        assertThrows(UnsupportedOperationException.class, () -> isSet.add("TesT"));
    }
}