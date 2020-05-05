package de.timmi6790.statsbotdiscord.utilities;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.Optional;
import java.util.regex.Pattern;

public class UtilitiesDiscord {
    private static final Pattern PATTERN_MENTION = Pattern.compile("@(everyone|here|[!&]?[0-9]{17,21})");

    public static EmbedBuilder getDefaultEmbedBuilder(final CommandParameters commandParameters) {
        return getDefaultEmbedBuilder(commandParameters.getEvent().getAuthor(), commandParameters.getEvent().getMemberOptional());
    }

    public static EmbedBuilder getDefaultEmbedBuilder(final User user, final Optional<Member> member) {
        return new EmbedBuilder()
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setColor(member.isPresent() ? member.get().getColor() : Color.MAGENTA);
    }

    public static String escapeMentions(final String string) {
        return UtilitiesDiscord.PATTERN_MENTION.matcher(string).replaceAll("@\\u200b\\\\1");
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
