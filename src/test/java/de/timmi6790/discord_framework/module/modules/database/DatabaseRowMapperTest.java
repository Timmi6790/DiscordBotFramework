package de.timmi6790.discord_framework.module.modules.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DatabaseRowMapperTest {
    private final TestMapper mapper = new TestMapper();

    @ParameterizedTest
    @ValueSource(strings = {"", " ", ",", "1,2", "1,2,3", "AAAAAAAAAAA", "AAAAAAAAAA,DDDDDDDD", "AAAA;DDDDD,cccccccc"})
    void toSetString(final String value) {
        final Set<String> shouldSet = new HashSet<>(Arrays.asList(value.split(",")));
        final Set<String> isSet = this.mapper.toSet(value, String::valueOf);

        assertThat(isSet).isEqualTo(shouldSet);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1,2", "1,2,3", "1", "12331313,1233214444"})
    void toSetInt(final String value) {
        final Set<Integer> shouldSet = new HashSet<>();
        for (final String part : value.split(",")) {
            shouldSet.add(Integer.parseInt(part));
        }

        final Set<Integer> isSet = this.mapper.toSet(value, Integer::parseInt);

        assertThat(isSet).isEqualTo(shouldSet);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Key,1;Key1,2;Key2,3;Key3,4", "D,2;A,222222;C,100000"})
    void toMap(final String value) {
        final Map<String, Integer> shouldMap = new HashMap<>();
        for (final String entry : value.split(";")) {
            final String[] parts = entry.split(",");
            shouldMap.put(parts[0], Integer.parseInt(parts[1]));
        }

        final Map<String, Integer> isMap = this.mapper.toMap(value, String::valueOf, Integer::parseInt);

        assertThat(isMap).isEqualTo(shouldMap);
    }

    @Test
    void toMapNull() {
        final Map<String, Integer> isMap = this.mapper.toMap(null, String::valueOf, Integer::parseInt);
        assertThat(isMap).isEmpty();
    }

    private static class TestMapper extends DatabaseRowMapper {
    }
}