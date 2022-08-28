package de.timmi6790.discord_framework.module.modules.slashcommand.cause;

public enum BaseCommandCause implements CommandCause {
    MESSAGE,
    EMOTES;

    @Override
    public String getReason() {
        return this.name();
    }
}
