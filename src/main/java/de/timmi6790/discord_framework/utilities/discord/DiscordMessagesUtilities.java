package de.timmi6790.discord_framework.utilities.discord;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@UtilityClass
public class DiscordMessagesUtilities {
    protected final Color DEFAULT_EMBED_COLOUR = Color.MAGENTA;

    /**
     * Gets the default embed builder. With the author of the user and the colour is either the member guild colour of
     * his highest role or for private messages Color.MAGENTA
     *
     * @param commandParameters the command parameters
     * @return the embed builder
     */
    public MultiEmbedBuilder getEmbedBuilder(final @NonNull CommandParameters commandParameters) {
        return getEmbedBuilder(
                commandParameters.getUser(),
                commandParameters.isGuildCommand() ? commandParameters.getGuildMember() : null
        );
    }


    /**
     * Gets the default embed builder. With the author of the user and the colour is either the member guild colour of
     * his highest role or for private messages Color.MAGENTA
     *
     * @param user   the wanted user
     * @param member the guild member, null for private messages
     * @return the embed builder
     */
    public MultiEmbedBuilder getEmbedBuilder(final @NonNull User user, @Nullable final Member member) {
        return new MultiEmbedBuilder()
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setColor(member != null ? member.getColor() : DEFAULT_EMBED_COLOUR);
    }

    /**
     * Sends the embedBuilder to the user private message.
     *
     * @param user         the user
     * @param embedBuilder the embed builder
     * @return will return false for bot users
     */
    public boolean sendPrivateMessage(final @NonNull User user, final @NonNull MultiEmbedBuilder embedBuilder) {
        // We can't send private messages to other bots
        if (user.isBot()) {
            return false;
        }

        user.openPrivateChannel().queue(privateChannel -> {
            for (final MessageEmbed message : embedBuilder.build()) {
                privateChannel.sendMessage(message).queue();
            }
        });

        return true;
    }

    /**
     * Send the embedBuilder into the give textChannel. The success consumer will be consumed on each individual message
     * after send.
     *
     * @param textChannel  the text channel
     * @param embedBuilder the embed builder
     * @param success      consumer after message was send.
     */
    public void sendMessage(final @NonNull MessageChannel textChannel,
                            final @NonNull MultiEmbedBuilder embedBuilder,
                            final @NonNull Consumer<Message> success) {
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
    public void sendMessage(final @NonNull MessageChannel textChannel,
                            final @NonNull MultiEmbedBuilder embedBuilder) {
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
    public void sendMessageTimed(@NonNull final MessageChannel textChannel,
                                 @NonNull final MultiEmbedBuilder embedBuilder,
                                 final long deleteAfterSeconds) {
        for (final MessageEmbed message : embedBuilder.build()) {
            textChannel
                    .sendMessage(message)
                    .delay(deleteAfterSeconds, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
    }

    public void sendEmoteMessage(@NonNull final CommandParameters commandParameters,
                                 @NonNull final MultiEmbedBuilder embedBuilder,
                                 @NonNull final Map<String, AbstractEmoteReaction> emotes) {
        commandParameters.getLowestMessageChannel()
                .sendMessage(
                        embedBuilder
                                .setFooter("↓ Click Me!")
                                .buildSingle()
                )
                .queue(message -> {
                    if (!emotes.isEmpty()) {
                        DiscordBot.getInstance()
                                .getModuleManager()
                                .getModuleOrThrow(EmoteReactionModule.class)
                                .addEmoteReactionMessage(
                                        message,
                                        new EmoteReactionMessage(
                                                emotes,
                                                commandParameters.getUser().getIdLong(),
                                                commandParameters.getLowestMessageChannel().getIdLong()
                                        )
                                );
                    }

                    message.delete()
                            .queueAfter(
                                    90,
                                    TimeUnit.SECONDS,
                                    null,
                                    new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)
                            );
                });
    }
}
