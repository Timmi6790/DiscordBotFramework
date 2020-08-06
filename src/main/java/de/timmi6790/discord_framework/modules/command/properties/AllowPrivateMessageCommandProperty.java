package de.timmi6790.discord_framework.modules.command.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandProperty;

public class AllowPrivateMessageCommandProperty extends CommandProperty<Boolean> {
    private final boolean allowPrivateMessage;

    public AllowPrivateMessageCommandProperty(final boolean allowPrivateMessage) {
        this.allowPrivateMessage = allowPrivateMessage;
    }

    @Override
    public Boolean getValue() {
        return this.allowPrivateMessage;
    }

    @Override
    public boolean onPermissionCheck(final AbstractCommand<?> command, final CommandParameters commandParameters) {
        return !this.allowPrivateMessage && !commandParameters.isGuildCommand();
    }
}
