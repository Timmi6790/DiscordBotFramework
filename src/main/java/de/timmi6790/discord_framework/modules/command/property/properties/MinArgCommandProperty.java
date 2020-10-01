package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.property.CommandProperty;
import lombok.Data;

@Data
public class MinArgCommandProperty implements CommandProperty<Integer> {
    private final int minArgs;

    @Override
    public Integer getValue() {
        return this.minArgs;
    }

    @Override
    public boolean onCommandExecution(final AbstractCommand command, final CommandParameters commandParameters) {
        if (this.minArgs > commandParameters.getArgs().length) {
            command.sendMissingArgsMessage(commandParameters);
            return false;
        }

        return true;
    }
}
