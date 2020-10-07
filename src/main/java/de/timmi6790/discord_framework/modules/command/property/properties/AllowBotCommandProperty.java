package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.property.CommandProperty;
import lombok.Data;

@Data
public class AllowBotCommandProperty implements CommandProperty<Boolean> {
    private final boolean allowBot;

    @Override
    public Boolean getValue() {
        return this.allowBot;
    }

    @Override
    public boolean onPermissionCheck(final AbstractCommand command, final CommandParameters commandParameters) {
        if (this.allowBot) {
            return true;
        }

        return !commandParameters.getUser().isBot();
    }
}
