package de.timmi6790.discord_framework.modules.command.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;

public class CommandCommand extends AbstractCommand<CommandModule> {
    public CommandCommand() {
        super("command", "Management", "Command control command", "<>");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        return null;
    }
}
