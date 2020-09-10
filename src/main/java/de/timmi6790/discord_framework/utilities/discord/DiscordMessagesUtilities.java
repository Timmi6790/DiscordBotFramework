package de.timmi6790.discord_framework.utilities.discord;

import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Discord messages utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordMessagesUtilities {
    /**
     * Gets the default embed builder.
     * With the author of the user and
     * the colour is either the member guild colour of his highest role or for private messages Color.MAGENTA
     *
     * @param commandParameters the command parameters
     * @return the embed builder
     */
    public static MultiEmbedBuilder getEmbedBuilder(final @NonNull CommandParameters commandParameters) {
        if (commandParameters.isGuildCommand()) {
            return getEmbedBuilder(commandParameters.getUser(), commandParameters.getGuildMember());
        }

        return getEmbedBuilder(commandParameters.getUser(), null);
    }


    /**
     * Gets the default embed builder.
     * With the author of the user and
     * the colour is either the member guild colour of his highest role or for private messages Color.MAGENTA
     *
     * @param user   the wanted user
     * @param member the guild member, null for private messages
     * @return the embed builder
     */
    public static MultiEmbedBuilder getEmbedBuilder(final @NonNull User user, final Member member) {
        return new MultiEmbedBuilder()
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setColor(member != null ? member.getColor() : Color.MAGENTA);
    }

    /**
     * Sends the embedBuilder to the user private message.
     *
     * @param user         the user
     * @param embedBuilder the embed builder
     */
    public static void sendPrivateMessage(final @NonNull User user, final @NonNull MultiEmbedBuilder embedBuilder) {
        // We can't send private messages to other bots
        if (user.isBot()) {
            return;
        }

        user.openPrivateChannel().queue(privateChannel -> {
            for (final MessageEmbed message : embedBuilder.build()) {
                privateChannel.sendMessage(message).queue();
            }
        });
    }

    /**
     * Send the embedBuilder into the give textChannel.
     * The success consumer will be consumed on each individual message after send.
     *
     * @param textChannel  the text channel
     * @param embedBuilder the embed builder
     * @param success      consumer after message was send.
     */
    public static void sendMessage(final @NonNull MessageChannel textChannel, final @NonNull MultiEmbedBuilder embedBuilder, final Consumer<Message> success) {
        for (final MessageEmbed message : embedBuilder.build()) {
            textChannel
                    .sendMessage(message)
                    .queue(success);
        }
    }

    /**
     * Send the embedBuilder into the give textChannel.
     *
     * @param textChannel  the text channel
     * @param embedBuilder the embed builder
     */
    public static void sendMessage(final @NonNull MessageChannel textChannel, final @NonNull MultiEmbedBuilder embedBuilder) {
        for (final MessageEmbed message : embedBuilder.build()) {
            textChannel
                    .sendMessage(message)
                    .queue();
        }
    }

    /**
     * Send the embedBuilder into the give textChannel and delete it after x seconds.
     *
     * @param textChannel        the text channel
     * @param embedBuilder       the embed builder
     * @param deleteAfterSeconds the delete timer in seconds
     */
    public static void sendMessageTimed(@NonNull final MessageChannel textChannel, @NonNull final MultiEmbedBuilder embedBuilder, final long deleteAfterSeconds) {
        for (final MessageEmbed message : embedBuilder.build()) {
            textChannel
                    .sendMessage(message)
                    .delay(deleteAfterSeconds, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
    }
}
