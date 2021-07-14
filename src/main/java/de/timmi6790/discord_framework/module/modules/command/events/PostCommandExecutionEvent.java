package de.timmi6790.discord_framework.module.modules.command.events;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode(callSuper = true)
@Getter
public class PostCommandExecutionEvent extends CommandExecutionEvent {
    private final CommandResult commandResult;
    private final long executionTimeInNano;

    public PostCommandExecutionEvent(final Command command,
                                     final CommandParameters commandParameters,
                                     final CommandResult commandResult,
                                     final long executionTimeInNano) {
        super(commandParameters.getJda(), command, commandParameters);

        this.commandResult = commandResult;
        this.executionTimeInNano = executionTimeInNano;
    }
}
