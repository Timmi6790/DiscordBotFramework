package de.timmi6790.discord_framework.module.modules.command.listeners;

import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.module.modules.reactions.emote.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.module.modules.reactions.emote.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter(value = AccessLevel.PROTECTED)
@AllArgsConstructor
@Log4j2
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

    protected Optional<AbstractCommand> getCommand(final String commandName,
                                                   final CommandParameters commandParameters) {
        // Always show the help command when only the main command is executed
        if (commandName.isEmpty()) {
            return Optional.of(this.helpCommand);
        }

        final Optional<AbstractCommand> commandOpt = this.commandModule.getCommand(commandName);
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
                // Make sure that we have enough perms to send this
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
            description.append(String.format(
                    "%s is not a valid command.%n Use the %s command or click the %s emote to see all commands",
                    MarkdownUtil.monospace(firstArg),
                    MarkdownUtil.bold(this.commandModule.getMainCommand() + this.helpCommand.getName()),
                    DiscordEmotes.FOLDER.getEmote()
            ));

        } else {
            description.append(String.format(
                    "%s is not a valid command.%n Is it possible that you wanted to write?%n%n",
                    MarkdownUtil.monospace(firstArg)
            ));

            for (int index = 0; similarCommands.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();
                final AbstractCommand similarCommand = similarCommands.get(index);

                description.append(
                        String.format(
                                "%s %s | %s %n",
                                emote,
                                MarkdownUtil.bold(similarCommand.getName()),
                                similarCommand.getDescription()
                        )
                );
                emotes.put(emote, new CommandEmoteReaction(similarCommand, commandParameters));
            }
            description.append('\n').append(DiscordEmotes.FOLDER.getEmote()).append(" All commands");
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

        // Check if the main command exists
        final Optional<String> parsedStart = getParsedStart(
                event.getMessage().getContentRaw(),
                this.getCommandModule().getMainCommandPattern()
        );
        if (!parsedStart.isPresent()) {
            return;
        }

        final Matcher spaceMatcher = FIRST_SPACE_PATTERN.matcher(parsedStart.get());
        // This should never happen
        if (!spaceMatcher.find()) {
            return;
        }

        final CommandParameters commandParameters = CommandParameters.of(event, spaceMatcher.group(2));
        final String commandName = spaceMatcher.group(1);
        this.getCommand(commandName, commandParameters)
                .ifPresent(abstractCommand -> abstractCommand.runCommand(commandParameters));
    }
}
