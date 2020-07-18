package de.timmi6790.statsbotdiscord.utilities.discord;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.datatypes.StatEmbedBuilder;
import de.timmi6790.statsbotdiscord.events.MessageReceivedIntEvent;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.HelpCommand;
import de.timmi6790.statsbotdiscord.modules.emotereaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.emotereaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emotereaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesString;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UtilitiesDiscordMessages {
    public static StatEmbedBuilder getEmbedBuilder(final CommandParameters commandParameters) {
        return getEmbedBuilder(commandParameters.getEvent().getAuthor(), commandParameters.getEvent().getMemberOptional());
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

    public static void sendTimedMessage(final CommandParameters commandParameters, final EmbedBuilder embedBuilder, final int deleteTime) {
        sendTimedMessage(commandParameters.getEvent(), embedBuilder, deleteTime);
    }

    public static void sendTimedMessage(final MessageReceivedIntEvent event, final EmbedBuilder embedBuilder, final int deleteTime) {
        event.getChannel()
                .sendMessage(embedBuilder.build())
                .delay(deleteTime, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();
    }

    public static void sendIncorrectCommandHelpMessage(final MessageReceivedIntEvent event, final List<AbstractCommand> similarCommands, final String firstArg, final CommandParameters commandParameters) {
        final StringBuilder description = new StringBuilder();
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        if (similarCommands.isEmpty()) {
            description.append(MarkdownUtil.monospace(firstArg)).append(" is not a valid command.\n")
                    .append("Use the ").append(MarkdownUtil.bold(StatsBot.getCommandManager().getMainCommand() + " help")).append(" command or click the ")
                    .append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all commands.");

        } else {
            description.append(MarkdownUtil.monospace(firstArg)).append(" is not a valid command.\n")
                    .append("Is it possible that you wanted to write?\n\n");

            IntStream.range(0, similarCommands.size())
                    .forEach(index -> {
                        final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();
                        final AbstractCommand similarCommand = similarCommands.get(index);

                        description.append(emote).append(" ").append(MarkdownUtil.bold(similarCommand.getName())).append(" | ").append(similarCommand.getDescription()).append("\n");
                        emotes.put(emote, new CommandEmoteReaction(similarCommand, commandParameters));
                    });

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(" All commands");
        }
        StatsBot.getCommandManager().getCommand(HelpCommand.class).ifPresent(helpCommand -> emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(helpCommand, commandParameters)));

        final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, event.getAuthor().getIdLong(), event.getChannel().getIdLong());
        event.getChannel().sendMessage(
                UtilitiesDiscordMessages.getEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("Invalid Command")
                        .setDescription(description)
                        .setFooter("↓ Click Me!")
                        .build())
                .queue(sendMessage -> {
                            StatsBot.getEmoteReactionManager().addEmoteReactionMessage(sendMessage, emoteReactionMessage);
                            sendMessage.delete().queueAfter(90, TimeUnit.SECONDS);
                        }
                );
    }

    public static void sendHelpMessage(final CommandParameters commandParameters, final String userArg, final int argPos, final String argName,
                                       final AbstractCommand thisCommand, final AbstractCommand allCommand, final String[] newArgs, final List<String> similarNames) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder();
        description.append(MarkdownUtil.monospace(userArg)).append(" is not a valid ").append(argName).append(".\n");

        if (similarNames.isEmpty() && allCommand != null) {
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

            if (allCommand != null) {
                description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(MarkdownUtil.bold("All " + argName + "s"));
            }
        }

        final CommandParameters newCommandParameters = new CommandParameters(commandParameters);
        newCommandParameters.setArgs(newArgs);

        if (allCommand != null) {
            emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(allCommand, newCommandParameters));
        }

        sendEmoteMessage(commandParameters, "Invalid " + UtilitiesString.capitalize(argName), description.toString(), emotes);
    }

    public static void sendEmoteMessage(final CommandParameters commandParameters, final String title, final String description, final Map<String, AbstractEmoteReaction> emotes) {
        sendEmoteMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle(title)
                        .setDescription(description),
                emotes
        );
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

        UtilitiesDiscordMessages.sendPrivateMessage(
                event.getAuthor(),
                UtilitiesDiscordMessages.getEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("Missing Permission")
                        .setDescription("The bot is missing " + perms + " permission(s).")
        );
    }

    public static void sendUserBanMessage(final MessageReceivedIntEvent event) {
        UtilitiesDiscordMessages.sendPrivateMessage(
                event.getAuthor(),
                UtilitiesDiscordMessages.getEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("You are banned")
                        .setDescription("You are banned from using this service.")
        );
    }

    public static void sendGuildBanMessage(final MessageReceivedIntEvent event) {
        sendTimedMessage(
                event,
                UtilitiesDiscordMessages.getEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("Banned Server")
                        .setDescription("This server is banned from using this service."),
                90
        );
    }
}
