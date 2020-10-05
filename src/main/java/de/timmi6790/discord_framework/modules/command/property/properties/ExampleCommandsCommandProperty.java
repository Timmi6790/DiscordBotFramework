package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.property.CommandProperty;

public class ExampleCommandsCommandProperty implements CommandProperty<String[]> {
    private final String[] exampleCommands;

    public ExampleCommandsCommandProperty(final String... exampleCommands) {
        this.exampleCommands = exampleCommands.clone();
    }

    @Override
    public String[] getValue() {
        return this.exampleCommands.clone();
    }
}
