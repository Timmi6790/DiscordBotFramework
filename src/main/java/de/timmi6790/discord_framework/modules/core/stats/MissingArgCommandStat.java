package de.timmi6790.discord_framework.modules.core.stats;

import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class MissingArgCommandStat extends AbstractStat {
    public MissingArgCommandStat() {
        super("MissingArgs Commands", "core.stat.missing_arg_command");
    }

    @SubscribeEvent
    public void onCommandExecution(final CommandExecutionEvent.Post commandExecutionEvent) {
        if (commandExecutionEvent.getCommandResult() == CommandResult.MISSING_ARGS) {
            this.increaseStat(commandExecutionEvent.getParameters().getUserDb());
        }
    }
}
