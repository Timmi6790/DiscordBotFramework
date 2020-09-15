package de.timmi6790.discord_framework.utilities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class EnumUtilitiesTest {
    @Test
    void getPrettyNames() {
        final List<String> shouldValues = new ArrayList<>();
        for (final TestEnum testEnum : TestEnum.values()) {
            shouldValues.add(EnumUtilities.getPrettyName(testEnum));
        }

        assertThat(EnumUtilities.getPrettyNames(TestEnum.values())).isEqualTo(shouldValues);
    }

    @Test
    void getPrettyName() {
        assertThat(EnumUtilities.getPrettyName(TestEnum.TEST)).isEqualTo("Test");

        assertThat(EnumUtilities.getPrettyName(TestEnum.TEST_VALUE)).isEqualTo("TestValue");

        assertThat(EnumUtilities.getPrettyName(TestEnum.REALLY_LONG_TEST_VALUE)).isEqualTo("ReallyLongTestValue");
    }

    @Test
    void getIgnoreCase() {
        // Should find
        assertThat(EnumUtilities.getIgnoreCase("test", TestEnum.values()))
                .isPresent()
                .hasValue(TestEnum.TEST);

        assertThat(EnumUtilities.getIgnoreCase("testvalue", TestEnum.values()))
                .isPresent()
                .hasValue(TestEnum.TEST_VALUE);

        assertThat(EnumUtilities.getIgnoreCase("TesTValue", TestEnum.values()))
                .isPresent()
                .hasValue(TestEnum.TEST_VALUE);

        // Should not find
        assertThat(EnumUtilities.getIgnoreCase("Tesd", TestEnum.values()))
                .isNotPresent();

        assertThat(EnumUtilities.getIgnoreCase("", TestEnum.values()))
                .isNotPresent();
    }

    private enum TestEnum {
        TEST,
        TEST1,
        TEST3,
        VALUE,
        TEST_VALUE,
        LONG_TEST_VALUE,
        REALLY_LONG_TEST_VALUE
    }
}