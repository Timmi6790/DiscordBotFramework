package de.timmi6790.discord_framework.modules.command.listeners;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter(value = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MessageListener {
    private static final Pattern FIRST_SPACE_PATTERN = Pattern.compile("^([\\S]*)\\s*(.*)$");

    private final CommandModule commandModule;
    private final GuildDbModule guildDbModule;
    private final AbstractCommand helpCommand;

    protected static Optional<String> getParsedStart(@NonNull final String rawMessage,
                                                     @NonNull final Pattern mainCommandPattern) {
        // Check if the message matches the main or guild specific start regex
        final Matcher mainMatcher = mainCommandPattern.matcher(rawMessage);
        if (mainMatcher.find()) {
            return Optional.of(mainMatcher.group(1).trim());
        }

        return Optional.empty();
    }

    protected Optional<AbstractCommand> getCommand(final String commandName, final CommandParameters commandParameters) {
        final Optional<AbstractCommand> commandOpt = commandName.isEmpty() ? Optional.of(this.helpCommand) : this.commandModule.getCommand(commandName);

        if (commandOpt.isPresent()) {
            return commandOpt;
        } else {
            final List<AbstractCommand> similarCommands = DataUtilities.getSimilarityList(
                    commandName,
                    this.getCommandModule().getCommandsWithPerms(commandParameters),
                    AbstractCommand::getName,
                    0.6,
                    3
            );
            if (!similarCommands.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
                return Optional.of(similarCommands.get(0));

            } else {
                if (AbstractCommand.hasRequiredDiscordPerms(commandParameters, EnumSet.noneOf(Permission.class))) {
                    this.sendIncorrectCommandHelpMessage(commandParameters, similarCommands, commandName);
                }
                return Optional.empty();
            }
        }
    }

    protected void sendIncorrectCommandHelpMessage(@NonNull final CommandParameters commandParameters,
                                                   @NonNull final List<AbstractCommand> similarCommands,
                                                   @NonNull final String firstArg) {
        final StringBuilder description = new StringBuilder();
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        if (similarCommands.isEmpty()) {
            description.append(MarkdownUtil.monospace(firstArg))
                    .append(" is not a valid command.\n")
                    .append("Use the ")
                    .append(MarkdownUtil.bold(this.commandModule.getMainCommand() + this.helpCommand.getName()))
                    .append(" command or click the ")
                    .append(DiscordEmotes.FOLDER.getEmote())
                    .append(" emote to see all commands.");

        } else {
            description.append(MarkdownUtil.monospace(firstArg))
                    .append(" is not a valid command.\n")
                    .append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarCommands.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();
                final AbstractCommand similarCommand = similarCommands.get(index);

                description.append(emote)
                        .append(" ")
                        .append(MarkdownUtil.bold(similarCommand.getName()))
                        .append(" | ")
                        .append(similarCommand.getDescription())
                        .append("\n");
                emotes.put(emote, new CommandEmoteReaction(similarCommand, commandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(" All commands");
        }

        emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(this.helpCommand, commandParameters));
        DiscordMessagesUtilities.sendEmoteMessage(
                commandParameters,
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Command")
                        .setDescription(description)
                        .setFooter("↓ Click Me!"),
                emotes
        );
    }

    @SubscribeEvent
    public void onTextMessage(@NonNull final MessageReceivedEvent event) {
        // Ignore yourself
        if (this.getCommandModule().getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        final long guildId = event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0;
        // Make sure that the guild is created
        this.getGuildDbModule().getOrCreate(guildId);

        final Optional<String> parsedStart = getParsedStart(
                event.getMessage().getContentRaw(),
                this.getCommandModule().getMainCommandPattern()
        );
        if (!parsedStart.isPresent()) {
            return;
        }

        final Matcher spaceMatcher = FIRST_SPACE_PATTERN.matcher(parsedStart.get());
        if (!spaceMatcher.find()) {
            return;
        }

        final String rawArgs = spaceMatcher.group(2);
        final CommandParameters commandParameters = CommandParameters.of(event, rawArgs);
        if (AbstractCommand.isServerBanned(commandParameters) || AbstractCommand.isUserBanned(commandParameters)) {
            return;
        }

        final String commandName = spaceMatcher.group(1);
        try {
            this.getCommand(commandName, commandParameters)
                    .ifPresent(abstractCommand -> abstractCommand.runCommand(commandParameters));
        } catch (final Exception e) {
            DiscordBot.getLogger().error(e);
        }
    }
}
