package de.timmi6790.statsbotdiscord.modules.core.stats;

import de.timmi6790.statsbotdiscord.events.CommandExecutionEvent;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.eventhandler.SubscribeEvent;
import de.timmi6790.statsbotdiscord.modules.stat.AbstractStat;

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
