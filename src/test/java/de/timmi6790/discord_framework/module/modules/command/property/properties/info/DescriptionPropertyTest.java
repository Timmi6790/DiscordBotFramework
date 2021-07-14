package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DescriptionPropertyTest {
    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test with space", ""})
    void getValue(final String description) {
        final DescriptionProperty property = new DescriptionProperty(description);
        assertThat(property.getValue()).isEqualTo(description);
    }
}