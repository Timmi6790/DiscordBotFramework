package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import lombok.Data;

@Data
public class AllowBotProperty implements SlashCommandProperty<Boolean> {
    private final boolean allowBot;

    @Override
    public Boolean getValue() {
        return this.allowBot;
    }

    @Override
    public boolean onPermissionCheck(final SlashCommand command, final SlashCommandParameters commandParameters) {
        if (this.allowBot) {
            return true;
        }

        return !commandParameters.getUser().isBot();
    }
}
