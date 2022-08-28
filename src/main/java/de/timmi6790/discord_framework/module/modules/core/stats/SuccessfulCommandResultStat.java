package de.timmi6790.discord_framework.module.modules.core.stats;

import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import lombok.EqualsAndHashCode;

/**
 * A stat that tracks how often a command was executed with the command result {@link BaseCommandResult#SUCCESSFUL}
 */
@EqualsAndHashCode(callSuper = true)
public class SuccessfulCommandResultStat extends AbstractCommandResultStat {
    /**
     * Instantiates a new Successful command stat.
     */
    public SuccessfulCommandResultStat() {
        super("Successful Commands", BaseCommandResult.SUCCESSFUL);
    }
}
