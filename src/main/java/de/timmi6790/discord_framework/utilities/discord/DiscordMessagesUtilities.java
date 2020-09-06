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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordMessagesUtilities {
    public static MultiEmbedBuilder getEmbedBuilder(final @NonNull CommandParameters commandParameters) {
        if (commandParameters.isGuildCommand()) {
            return getEmbedBuilder(commandParameters.getUser(), commandParameters.getGuildMember());
        }

        return getEmbedBuilder(commandParameters.getUser(), null);
    }

    public static MultiEmbedBuilder getEmbedBuilder(final @NonNull User user, final Member member) {
        return new MultiEmbedBuilder()
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setColor(member != null ? member.getColor() : Color.MAGENTA);
    }

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

    public static void sendMessage(final @NonNull MessageChannel textChannel, final @NonNull MultiEmbedBuilder embedBuilder, final Consumer<Message> success) {
        for (final MessageEmbed message : embedBuilder.build()) {
            textChannel
                    .sendMessage(message)
                    .queue(success);
        }
    }

    public static void sendMessage(final @NonNull MessageChannel textChannel, final @NonNull MultiEmbedBuilder embedBuilder) {
        for (final MessageEmbed message : embedBuilder.build()) {
            textChannel
                    .sendMessage(message)
                    .queue();
        }
    }

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
