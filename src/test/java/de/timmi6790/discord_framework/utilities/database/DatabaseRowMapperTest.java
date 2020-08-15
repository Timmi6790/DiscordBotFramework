package de.timmi6790.discord_framework.utilities.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DatabaseRowMapperTest extends DatabaseRowMapper {
    @ParameterizedTest
    @ValueSource(strings = {"", " ", ",", "1,2", "1,2,3", "AAAAAAAAAAA", "AAAAAAAAAA,DDDDDDDD", "AAAA;DDDDD,cccccccc"})
    void toSetStringTest(final String value) {
        final Set<String> shouldSet = new HashSet<>(Arrays.asList(value.split(",")));
        final Set<String> isSet = this.toSet(value, String::valueOf);

        assertThat(isSet).isEqualTo(shouldSet);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1,2", "1,2,3", "1", "12331313,1233214444"})
    void toSetIntTest(final String value) {
        final Set<Integer> shouldSet = new HashSet<>();
        for (final String part : value.split(",")) {
            shouldSet.add(Integer.parseInt(part));
        }

        final Set<Integer> isSet = this.toSet(value, Integer::parseInt);

        assertThat(isSet).isEqualTo(shouldSet);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Key,1;Key1,2;Key2,3;Key3,4", "D,2;A,222222;C,100000"})
    void toMapTest(final String value) {
        final Map<String, Integer> shouldMap = new HashMap<>();
        for (final String entry : value.split(";")) {
            final String[] parts = entry.split(",");
            shouldMap.put(parts[0], Integer.parseInt(parts[1]));
        }

        final Map<String, Integer> isMap = this.toMap(value, String::valueOf, Integer::parseInt);

        assertThat(isMap).isEqualTo(shouldMap);

    }
}