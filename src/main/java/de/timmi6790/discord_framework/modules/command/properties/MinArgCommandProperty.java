package de.timmi6790.discord_framework.modules.command.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandProperty;

public class MinArgCommandProperty extends CommandProperty<Integer> {
    private final int minArgs;

    public MinArgCommandProperty(final int minArgs) {
        this.minArgs = minArgs;
    }

    @Override
    public Integer getValue() {
        return this.minArgs;
    }

    @Override
    public boolean onCommandExecution(final AbstractCommand<?> command, final CommandParameters commandParameters) {
        if (this.minArgs > commandParameters.getArgs().length) {
            command.sendMissingArgsMessage(commandParameters);
            return false;
        }

        return true;
    }
}
