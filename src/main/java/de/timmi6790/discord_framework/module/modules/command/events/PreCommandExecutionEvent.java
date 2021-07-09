package de.timmi6790.discord_framework.module.modules.command.events;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;

public class PreCommandExecutionEvent extends CommandExecutionEvent {
    public PreCommandExecutionEvent(final Command command,
                                    final CommandParameters commandParameters) {
        super(commandParameters.getJda(), command, commandParameters);
    }
}
