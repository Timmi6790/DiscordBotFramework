package de.timmi6790.discord_framework.module.modules.command.property.properties.info;


import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import lombok.Data;

@Data
public class ExampleCommandsProperty implements CommandProperty<String[]> {
    private final String[] exampleCommands;

    public ExampleCommandsProperty(final String... exampleCommands) {
        this.exampleCommands = exampleCommands.clone();
    }

    @Override
    public String[] getValue() {
        return this.exampleCommands.clone();
    }
}
