package de.timmi6790.discord_framework.utilities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DataUtilitiesTest {

    @Test
    void getSimilarityListTest() {
        final List<String> values = new ArrayList<>();
        Collections.addAll(
                values,
                "Example",
                "Example1",
                "Example2",
                "A",
                "A",
                "A"
        );

        for (char alphabet = 'a'; 'z' >= alphabet; alphabet++) {
            values.add(String.valueOf(alphabet));
        }

        final List<String> findExample = DataUtilities.getSimilarityList("Example", values, 1, 1);
        assertThat(findExample).hasSize(1);
        assertThat(findExample.get(0)).isEqualTo("Example");

        final List<String> findA = DataUtilities.getSimilarityList("A", values, 0, 2);
        assertThat(findA).hasSize(2);
        assertThat(findA.get(0)).isEqualTo("A");

        final List<String> findAZ = DataUtilities.getSimilarityList("AZ", values, 1, 1);
        assertThat(findAZ).isEmpty();
    }
}