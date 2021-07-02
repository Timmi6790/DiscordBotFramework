package de.timmi6790.discord_framework.module.modules.command_old.property.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ExampleCommandsCommandPropertyTest {

    @Test
    void getValue() {
        final String[] input = {"d", "a", "c", "d"};
        final ExampleCommandsCommandProperty property = new ExampleCommandsCommandProperty(input);
        assertThat(property.getValue()).isEqualTo(input);
    }
}