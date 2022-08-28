package de.timmi6790.discord_framework.module.modules.slashcommand.events;


import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode(callSuper = true)
@Getter
public class PostCommandExecutionEvent extends CommandExecutionEvent {
    private final CommandResult commandResult;
    private final long executionTimeInNano;

    public PostCommandExecutionEvent(final SlashCommand command,
                                     final SlashCommandParameters commandParameters,
                                     final CommandResult commandResult,
                                     final long executionTimeInNano) {
        super(commandParameters.getJda(), command, commandParameters);

        this.commandResult = commandResult;
        this.executionTimeInNano = executionTimeInNano;
    }
}
