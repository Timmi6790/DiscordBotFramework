package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import lombok.NonNull;

@Data
public class AllowPrivateMessageCommandProperty implements CommandProperty<Boolean> {
    private final boolean allowPrivateMessage;

    @Override
    public Boolean getValue() {
        return this.allowPrivateMessage;
    }

    @Override
    public boolean onCommandExecution(@NonNull final AbstractCommand command, @NonNull final CommandParameters commandParameters) {
        final boolean success = !this.allowPrivateMessage && !commandParameters.isGuildCommand();
        if (!success) {
            DiscordMessagesUtilities.sendMessageTimed(
                    commandParameters.getLowestMessageChannel(),
                    DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                            .setTitle("Error")
                            .appendDescription("You can not execute this command in your private messages with this bot."),
                    90
            );
        }

        return success;
    }
}
