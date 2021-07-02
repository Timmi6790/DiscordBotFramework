package de.timmi6790.discord_framework.module.modules.core.stats;

import de.timmi6790.discord_framework.module.modules.command_old.CommandResult;
import lombok.EqualsAndHashCode;

/**
 * A stat that tracks how often a command was executed with the command result {@link CommandResult#SUCCESS}
 */
@EqualsAndHashCode(callSuper = true)
public class SuccessfulCommandResultStat extends AbstractCommandResultStat {
    /**
     * Instantiates a new Successful command stat.
     */
    public SuccessfulCommandResultStat() {
        super("Successful Commands", CommandResult.SUCCESS);
    }
}
