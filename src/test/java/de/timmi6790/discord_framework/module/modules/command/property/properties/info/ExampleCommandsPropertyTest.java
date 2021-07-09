package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleCommandsPropertyTest {
    @Test
    void getValue() {
        final String[] exampleCommands = new String[]{
                "test1 test2 test3",
                "two",
                "three"
        };

        final ExampleCommandsProperty property = new ExampleCommandsProperty(exampleCommands);
        assertThat(property.getValue()).isEqualTo(exampleCommands);
    }
}