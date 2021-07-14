package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import lombok.Data;

@Data
public class AllowBotProperty implements CommandProperty<Boolean> {
    private final boolean allowBot;

    @Override
    public Boolean getValue() {
        return this.allowBot;
    }

    @Override
    public boolean onPermissionCheck(final Command command, final CommandParameters commandParameters) {
        if (this.allowBot) {
            return true;
        }

        return !commandParameters.getUser().isBot();
    }
}
