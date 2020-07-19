package de.timmi6790.discord_framework.modules.core.stats;

import de.timmi6790.discord_framework.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.event_handler.SubscribeEvent;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;

public class SuccessfulCommandStat extends AbstractStat {
    public SuccessfulCommandStat() {
        super("Successful Commands", "core.stat.successful_command");
    }

    @SubscribeEvent
    public void onCommandExecution(final CommandExecutionEvent.Post commandExecutionEvent) {
        if (commandExecutionEvent.getCommandResult() == CommandResult.SUCCESS) {
            this.increaseStat(commandExecutionEvent.getParameters().getUserDb());
        }
    }
}
