package de.timmi6790.discord_framework.modules.core.stats;

import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A stat that tracks how often a command was executed with the given command result
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractCommandResultStat extends AbstractStat {
    /**
     * The Required command result for the stat to increase
     */
    private final CommandResult requiredCommandResult;

    /**
     * Instantiates a new command result stat.
     *
     * @param statName              the stat name
     * @param requiredCommandResult the required command result
     */
    protected AbstractCommandResultStat(final String statName, final CommandResult requiredCommandResult) {
        super(statName);

        this.requiredCommandResult = requiredCommandResult;
    }

    /**
     * On command execution post
     *
     * @param commandExecutionEvent the command execution event
     */
    @SubscribeEvent
    public void onCommandExecution(final CommandExecutionEvent.Post commandExecutionEvent) {
        if (commandExecutionEvent.getCommandResult() == this.requiredCommandResult) {
            this.increaseStat(commandExecutionEvent.getParameters().getUserDb());
        }
    }
}
