package de.timmi6790.discord_framework.module.modules.command.property.properties;

import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class AllowBotCommandProperty implements CommandProperty<Boolean> {
    private final boolean allowBot;

    @Override
    public Boolean getValue() {
        return this.allowBot;
    }

    @Override
    public boolean onPermissionCheck(final @NotNull AbstractCommand command, final @NotNull CommandParameters commandParameters) {
        if (this.allowBot) {
            return true;
        }

        return !commandParameters.getUser().isBot();
    }
}
