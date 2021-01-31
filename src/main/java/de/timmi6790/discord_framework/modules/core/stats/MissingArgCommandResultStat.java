package de.timmi6790.discord_framework.modules.core.stats;

import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.EqualsAndHashCode;

/**
 * A stat that tracks how often a command was executed with the command result {@link CommandResult#MISSING_ARGS}
 */
@EqualsAndHashCode(callSuper = true)
public class MissingArgCommandResultStat extends AbstractCommandResultStat {
    /**
     * Instantiates a new Missing arg command stat.
     */
    public MissingArgCommandResultStat() {
        super("MissingArgs Commands", CommandResult.MISSING_ARGS);
    }
}
