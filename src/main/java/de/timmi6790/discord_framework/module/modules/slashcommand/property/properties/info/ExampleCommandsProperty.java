package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info;


import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import lombok.Data;

@Data
public class ExampleCommandsProperty implements SlashCommandProperty<String[]> {
    private final String[] exampleCommands;

    public ExampleCommandsProperty(final String... exampleCommands) {
        this.exampleCommands = exampleCommands.clone();
    }

    @Override
    public String[] getValue() {
        return this.exampleCommands.clone();
    }
}
