package de.timmi6790.discord_framework.module.modules.slashcommand.result;

public enum BaseCommandResult implements CommandResult {
    SUCCESSFUL,
    INVALID_ARGS,
    INVALID_SUB_COMMAND,
    EXCEPTION,
    ERROR,
    FAIL,
    MISSING_ARGS,
    UNKNOWN;

    @Override
    public String getExitReason() {
        return this.name();
    }
}
