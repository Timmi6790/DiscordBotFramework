package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import lombok.NonNull;

@Data
public class AllowGuildMessageCommandProperty implements CommandProperty<Boolean> {
    private final boolean allowGuildMessage;

    protected void sendErrorMessage(final CommandParameters commandParameters) {
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Error")
                        .appendDescription("You can not execute this command in a guild."),
                90
        );
    }

    @Override
    public Boolean getValue() {
        return this.allowGuildMessage;
    }

    @Override
    public boolean onCommandExecution(@NonNull final AbstractCommand command, @NonNull final CommandParameters commandParameters) {
        if (this.allowGuildMessage) {
            return true;
        }

        final boolean guildMessage = commandParameters.isGuildCommand();
        if (guildMessage) {
            this.sendErrorMessage(commandParameters);
        }

        return !guildMessage;
    }
}
