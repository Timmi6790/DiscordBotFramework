package de.timmi6790.discord_framework.modules.core.stats;

import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.EqualsAndHashCode;

/**
 * A stat that tracks how often a command was executed with the command result {@link CommandResult#INVALID_ARGS}
 */
@EqualsAndHashCode(callSuper = true)
public class IncorrectArgCommandResultStat extends AbstractCommandResultStat {
    /**
     * Instantiates a new Incorrect arg command stat.
     */
    public IncorrectArgCommandResultStat() {
        super("IncorrectArgs Commands", CommandResult.INVALID_ARGS);
    }
}
