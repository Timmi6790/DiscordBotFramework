package de.timmi6790.discord_framework.module.modules.slashcommand.events;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;

public class PreCommandExecutionEvent extends CommandExecutionEvent {
    public PreCommandExecutionEvent(final SlashCommand command,
                                    final SlashCommandParameters commandParameters) {
        super(commandParameters.getJda(), command, commandParameters);
    }
}
