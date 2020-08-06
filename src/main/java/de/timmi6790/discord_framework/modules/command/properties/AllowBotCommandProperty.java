package de.timmi6790.discord_framework.modules.command.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandProperty;

public class AllowBotCommandProperty extends CommandProperty<Boolean> {
    private final boolean allowBot;

    public AllowBotCommandProperty(final boolean allowBot) {
        this.allowBot = allowBot;
    }

    @Override
    public Boolean getValue() {
        return this.allowBot;
    }

    @Override
    public boolean onPermissionCheck(final AbstractCommand<?> command, final CommandParameters commandParameters) {
        return !this.allowBot && commandParameters.getUser().isBot();
    }
}
