package de.timmi6790.discord_framework.utilities.discord;

import de.timmi6790.discord_framework.datatypes.StatEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.Optional;

public class DiscordMessagesUtilities {
    public static StatEmbedBuilder getEmbedBuilder(final CommandParameters commandParameters) {
        if (commandParameters.isGuildCommand()) {
            return getEmbedBuilder(commandParameters.getUser(), Optional.ofNullable(commandParameters.getGuild().retrieveMember(commandParameters.getUser(), false).complete()));
        }

        return getEmbedBuilder(commandParameters.getUser(), Optional.empty());
    }

    public static StatEmbedBuilder getEmbedBuilder(final User user, final Optional<Member> member) {
        return new StatEmbedBuilder()
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setColor(member.isPresent() ? member.get().getColor() : Color.MAGENTA);
    }

    public static void sendPrivateMessage(final User user, final EmbedBuilder embedBuilder) {
        // We can't send private messages to other bots
        if (user.isBot()) {
            return;
        }

        user.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(embedBuilder.build()))
                .queue();
    }
}
