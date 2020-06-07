package de.timmi6790.statsbotdiscord.modules.core.stats;

import de.timmi6790.statsbotdiscord.events.CommandExecutionEvent;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.eventhandler.SubscribeEvent;
import de.timmi6790.statsbotdiscord.modules.stat.AbstractStat;

public class IncorrectArgCommandStat extends AbstractStat {
    public IncorrectArgCommandStat() {
        super("IncorrectArgs Commands", "core.stat.incorrect_args_command");
    }

    @SubscribeEvent
    public void onCommandExecution(final CommandExecutionEvent.Post commandExecutionEvent) {
        if (commandExecutionEvent.getCommandResult() == CommandResult.INVALID_ARGS) {
            this.increaseStat(commandExecutionEvent.getParameters().getUserDb());
        }
    }
}
