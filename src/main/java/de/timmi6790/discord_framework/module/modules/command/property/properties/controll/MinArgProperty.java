package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.ExampleCommandsProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.MessageUtilities;
import lombok.Data;

@Data
public class MinArgProperty implements CommandProperty<Byte> {
    private final byte minArgs;

    public MinArgProperty(final int minArgs) {
        // Assure that the input is a valid positive byte
        // I decided against a byte constructor because we need to check the smaller than 0 anyway and the cast looks ugly.
        if (minArgs <= 0 || minArgs >= 128) {
            throw new IllegalArgumentException("MinArgs must be between than 1 and 127");
        }

        this.minArgs = (byte) minArgs;
    }

    @Override
    public Byte getValue() {
        return this.minArgs;
    }

    @Override
    public boolean onCommandExecution(final Command command, final CommandParameters commandParameters) {
        if (this.minArgs > commandParameters.getArgs().length) {
            MessageUtilities.sendMissingArgsMessage(
                    commandParameters,
                    command.getPropertyValueOrDefault(SyntaxProperty.class, () -> ""),
                    this.minArgs,
                    command.getPropertyValueOrDefault(ExampleCommandsProperty.class, () -> new String[0])
            );
            return false;
        }

        return true;
    }
}
