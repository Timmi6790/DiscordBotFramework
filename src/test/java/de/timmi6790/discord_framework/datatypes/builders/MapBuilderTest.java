package de.timmi6790.discord_framework.datatypes.builders;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBuilderTest {
    private static final Map<String, String> TEST_MAP = new HashMap<>();

    @BeforeAll
    static void setUp() {
        TEST_MAP.put("1", "d");
        TEST_MAP.put("AAAA", "DDDD");
        TEST_MAP.put("AA ", "DD ");
        TEST_MAP.put("AA D", "D    D ");
        TEST_MAP.put(" D ", "CCC");
    }

    @Test
    void ofHashMap() {
        final Map<String, String> isMap = MapBuilder.<String, String>ofHashMap().build();
        assertThat(isMap).isInstanceOf(HashMap.class);
    }

    @Test
    void ofHashMapSize() {
        final Map<String, String> isMap = MapBuilder.<String, String>ofHashMap(2).build();
        assertThat(isMap).isInstanceOf(HashMap.class);
    }

    @Test
    void put() {
        final MapBuilder<String, String> mapBuilder = MapBuilder.ofHashMap();
        for (final Map.Entry<String, String> testEntry : TEST_MAP.entrySet()) {
            mapBuilder.put(testEntry.getKey(), testEntry.getValue());
        }
        final Map<String, String> isMap = mapBuilder.build();

        assertThat(isMap).hasSize(TEST_MAP.size()).containsAllEntriesOf(TEST_MAP);
    }

    @Test
    void putIfCondition() {
        final Map<String, String> addedMap = new HashMap<>();

        final MapBuilder<String, String> mapBuilder = MapBuilder.ofHashMap();
        for (final Map.Entry<String, String> testEntry : TEST_MAP.entrySet()) {
            final boolean added = ThreadLocalRandom.current().nextBoolean();
            if (added) {
                addedMap.put(testEntry.getKey(), testEntry.getValue());
            }

            mapBuilder.put(testEntry.getKey(), testEntry.getValue(), added);
        }
        final Map<String, String> isMap = mapBuilder.build();

        assertThat(isMap).hasSize(addedMap.size()).containsAllEntriesOf(addedMap);
    }

    @Test
    void putAll() {
        final Map<String, String> isMap = MapBuilder.<String, String>ofHashMap()
                .putAll(TEST_MAP)
                .build();

        assertThat(isMap).hasSize(TEST_MAP.size()).containsAllEntriesOf(TEST_MAP);
    }

    @Test
    void putAllIfCondition() {
        final Map<String, String> addedMap = new HashMap<>();

        final Map<String, String> isMap = MapBuilder.<String, String>ofHashMap()
                .putAll(TEST_MAP, (s, s2) -> {
                    final boolean added = ThreadLocalRandom.current().nextBoolean();
                    if (added) {
                        addedMap.put(s, s2);
                    }
                    return added;
                })
                .build();

        assertThat(isMap).hasSize(addedMap.size()).containsAllEntriesOf(addedMap);
    }

    @Test
    void build() {
        final Map<String, String> otherTestValues = new HashMap<>();
        otherTestValues.put("---", "D");
        otherTestValues.put("d_-dd", "DDD");

        final Map<String, String> isMap = MapBuilder.<String, String>ofHashMap()
                .putAll(TEST_MAP)
                .build();
        isMap.putAll(otherTestValues);

        assertThat(isMap).hasSize(TEST_MAP.size() + otherTestValues.size())
                .containsAllEntriesOf(TEST_MAP)
                .containsAllEntriesOf(otherTestValues);
    }

    @Test
    void buildUnmodifiable() {
        final Map<String, String> isMap = MapBuilder.<String, String>ofHashMap()
                .putAll(TEST_MAP)
                .buildUnmodifiable();
        assertThrows(UnsupportedOperationException.class, () -> isMap.put("TesT", "D"));
    }
}