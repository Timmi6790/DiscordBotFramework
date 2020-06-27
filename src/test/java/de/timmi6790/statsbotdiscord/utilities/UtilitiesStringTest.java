package de.timmi6790.statsbotdiscord.utilities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class UtilitiesStringTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", " test", "test", "test dsad s asdas sa asddasads ", "Hello", "HeLLO"})
    void testCapitalize(final String value) {
        if (value.isEmpty()) {
            return;
        }

        final String result = UtilitiesString.capitalize(value);
        assertThat(result).isEqualTo(value.substring(0, 1).toUpperCase() + value.substring(1));
    }
}
