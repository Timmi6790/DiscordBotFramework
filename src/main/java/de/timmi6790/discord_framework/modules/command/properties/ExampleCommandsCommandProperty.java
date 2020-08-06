package de.timmi6790.discord_framework.modules.command.properties;

import de.timmi6790.discord_framework.modules.command.CommandProperty;

public class ExampleCommandsCommandProperty extends CommandProperty<String[]> {
    private final String[] exampleCommands;

    public ExampleCommandsCommandProperty(final String... exampleCommands) {
        this.exampleCommands = exampleCommands;
    }

    @Override
    public String[] getValue() {
        return this.exampleCommands;
    }
}
