package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import lombok.Data;

@Data
public class AllowPrivateMessageProperty implements SlashCommandProperty<Boolean> {
    private final boolean allowPrivateMessage;

    protected void sendErrorMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Error")
                        .appendDescription("You can not execute this command in your private messages with this bot.")
        );
    }

    @Override
    public Boolean getValue() {
        return this.allowPrivateMessage;
    }

    @Override
    public boolean onCommandExecution(final SlashCommand command, final SlashCommandParameters commandParameters) {
        if (this.allowPrivateMessage || commandParameters.isGuildCommand()) {
            return true;
        }

        this.sendErrorMessage(commandParameters);
        return false;
    }
}
