package de.timmi6790.discord_framework.module.modules.core.stats;

import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import lombok.EqualsAndHashCode;

/**
 * A stat that tracks how often a command was executed with the command result {@link BaseCommandResult#MISSING_ARGS}
 */
@EqualsAndHashCode(callSuper = true)
public class MissingArgCommandResultStat extends AbstractCommandResultStat {
    /**
     * Instantiates a new Missing arg command stat.
     */
    public MissingArgCommandResultStat() {
        super("MissingArgs Commands", BaseCommandResult.MISSING_ARGS);
    }
}
