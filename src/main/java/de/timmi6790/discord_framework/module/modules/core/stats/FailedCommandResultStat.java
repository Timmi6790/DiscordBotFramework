package de.timmi6790.discord_framework.module.modules.core.stats;

import de.timmi6790.discord_framework.module.modules.command_old.CommandResult;
import lombok.EqualsAndHashCode;

/**
 * A stat that tracks how often a command was executed with the command result {@link CommandResult#FAIL}
 */
@EqualsAndHashCode(callSuper = true)
public class FailedCommandResultStat extends AbstractCommandResultStat {
    /**
     * Instantiates a new Failed command stat.
     */
    public FailedCommandResultStat() {
        super("Failed Commands", CommandResult.FAIL);
    }
}
