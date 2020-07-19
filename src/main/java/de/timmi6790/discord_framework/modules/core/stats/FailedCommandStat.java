package de.timmi6790.discord_framework.modules.core.stats;

import de.timmi6790.discord_framework.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.event_handler.SubscribeEvent;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;

public class FailedCommandStat extends AbstractStat {
    public FailedCommandStat() {
        super("Failed Commands", "core.stat.failed_command");
    }

    @SubscribeEvent
    public void onCommandExecution(final CommandExecutionEvent.Post commandExecutionEvent) {
        if (commandExecutionEvent.getCommandResult() == CommandResult.FAIL) {
            this.increaseStat(commandExecutionEvent.getParameters().getUserDb());
        }
    }
}
