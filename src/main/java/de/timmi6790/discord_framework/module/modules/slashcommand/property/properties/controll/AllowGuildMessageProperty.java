package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import lombok.Data;

@Data
public class AllowGuildMessageProperty implements SlashCommandProperty<Boolean> {
    private final boolean allowGuildMessage;

    protected void sendErrorMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Error")
                        .appendDescription("You can not execute this command in a guild.")
        );
    }

    @Override
    public Boolean getValue() {
        return this.allowGuildMessage;
    }

    @Override
    public boolean onCommandExecution(final SlashCommand command, final SlashCommandParameters commandParameters) {
        if (this.allowGuildMessage || !commandParameters.isGuildCommand()) {
            return true;
        }

        this.sendErrorMessage(commandParameters);
        return false;
    }
}
