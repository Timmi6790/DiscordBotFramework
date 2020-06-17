package de.timmi6790.statsbotdiscord.utilities;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.datatypes.StatEmbedBuilder;
import de.timmi6790.statsbotdiscord.events.MessageReceivedIntEvent;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UtilitiesDiscord {
    private static final Pattern PATTERN_MENTION = Pattern.compile("@(everyone|here|[!&]?[0-9]{17,21})");

    public static StatEmbedBuilder getEmbedBuilder(final CommandParameters commandParameters) {
        return getEmbedBuilder(commandParameters.getEvent().getAuthor(), commandParameters.getEvent().getMemberOptional());
    }

    public static StatEmbedBuilder getEmbedBuilder(final User user, final Optional<Member> member) {
        return new StatEmbedBuilder()
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

    public static void sendHelpMessage(final CommandParameters commandParameters, final String userArg, final int argPos, final String argName,
                                       final AbstractCommand thisCommand, final AbstractCommand allCommand, final String[] newArgs, final List<String> similarNames) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder();
        description.append(MarkdownUtil.monospace(userArg)).append(" is not a valid ").append(argName).append(".\n");

        if (similarNames.isEmpty()) {
            description.append("Use the ").append(MarkdownUtil.bold(StatsBot.getCommandManager().getMainCommand() + " " + allCommand.getName() + " " + String.join(" ", newArgs)))
                    .append(" command or click the ").append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all ").append(argName).append("s.");

        } else {
            description.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarNames.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(emote).append(" ").append(MarkdownUtil.bold(similarNames.get(index))).append("\n");

                final CommandParameters newCommandParameters = new CommandParameters(commandParameters);
                newCommandParameters.getArgs()[argPos] = similarNames.get(index);

                emotes.put(emote, new CommandEmoteReaction(thisCommand, newCommandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(MarkdownUtil.bold("All " + argName + "s"));
        }

        final CommandParameters newCommandParameters = new CommandParameters(commandParameters);
        newCommandParameters.setArgs(newArgs);
        emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(allCommand, newCommandParameters));

        sendEmoteMessage(commandParameters, "Invalid " + UtilitiesString.capitalize(argName), description.toString(), emotes);
    }

    public static void sendEmoteMessage(final CommandParameters commandParameters, final String title, final String description, final Map<String, AbstractEmoteReaction> emotes) {
        sendEmoteMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle(title)
                        .setDescription(description),
                emotes);
    }

    public static void sendEmoteMessage(final CommandParameters commandParameters, final EmbedBuilder embedBuilder, final Map<String, AbstractEmoteReaction> emotes) {
        commandParameters.getEvent().getChannel().sendMessage(
                embedBuilder.setFooter("↓ Click Me!").build())
                .queue(message -> {
                            if (!emotes.isEmpty()) {
                                final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, commandParameters.getEvent().getAuthor().getIdLong(),
                                        commandParameters.getEvent().getChannel().getIdLong());
                                StatsBot.getEmoteReactionManager().addEmoteReactionMessage(message, emoteReactionMessage);
                            }

                            message.delete().queueAfter(90, TimeUnit.SECONDS);
                        }
                );
    }

    public static void sendMissingPermsMessage(final MessageReceivedIntEvent event, final List<Permission> missingPerms) {
        final String perms = missingPerms.stream()
                .map(perm -> MarkdownUtil.monospace(perm.getName()))
                .collect(Collectors.joining(","));

        UtilitiesDiscord.sendPrivateMessage(
                event.getAuthor(),
                UtilitiesDiscord.getEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("Missing Permission")
                        .setDescription("The bot is missing " + perms + " permission(s).")
        );
    }
}
