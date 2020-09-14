package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.GetModule;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.NonNull;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener extends GetModule<CommandModule> {
    private static final Pattern FIRST_SPACE_PATTERN = Pattern.compile("^([\\S]*)\\s*(.*)$");

    protected static Optional<String> getParsedStart(@NonNull final String rawMessage, @NonNull final Pattern mainCommandPattern, final Pattern guildCommandAliasPattern) {
        // Check if the message matches the main or guild specific start regex
        final Matcher mainMatcher = mainCommandPattern.matcher(rawMessage);
        if (mainMatcher.find()) {
            return Optional.of(mainMatcher.group(1).trim());
        }

        if (guildCommandAliasPattern != null) {
            final Matcher guildAliasMatcher = guildCommandAliasPattern.matcher(rawMessage);
            if (guildAliasMatcher.find()) {
                return Optional.of(guildAliasMatcher.group(1).trim());
            }
        }

        return Optional.empty();
    }

    @SubscribeEvent
    public void onTextMessage(@NonNull final MessageReceivedEvent event) {
        // Ignore yourself
        if (this.getModule().getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        final long guildId = event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0;
        final GuildDb guildDb = this.getModuleManager().getModuleOrThrow(GuildDbModule.class).getOrCreate(guildId);

        final Optional<String> parsedStart = getParsedStart(
                event.getMessage().getContentRaw(),
                this.getModule().getMainCommandPattern(),
                guildDb.getCommandAliasPattern().orElse(null)
        );
        if (!parsedStart.isPresent()) {
            return;
        }

        final Matcher spaceMatcher = FIRST_SPACE_PATTERN.matcher(parsedStart.get());
        if (!spaceMatcher.find()) {
            return;
        }

        final String commandName = spaceMatcher.group(1);
        final String rawArgs = spaceMatcher.group(2);

        final Optional<AbstractCommand<?>> commandOpt = commandName.isEmpty() ? this.getModule().getCommand(HelpCommand.class) : this.getModule().getCommand(commandName);

        final CommandParameters commandParameters = new CommandParameters(event, rawArgs);
        if (AbstractCommand.isServerBanned(commandParameters) || AbstractCommand.isUserBanned(commandParameters)) {
            return;
        }

        final AbstractCommand<?> command;
        if (commandOpt.isPresent()) {
            command = commandOpt.get();
        } else {
            final List<AbstractCommand<?>> similarCommands = this.getModule().getSimilarCommands(commandParameters, commandName, 0.6, 3);
            if (!similarCommands.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
                command = similarCommands.get(0);

            } else {
                if (AbstractCommand.hasRequiredDiscordPerms(commandParameters, EnumSet.noneOf(Permission.class))) {
                    this.sendIncorrectCommandHelpMessage(commandParameters, similarCommands, commandName);
                }
                return;
            }
        }

        try {
            command.runCommand(commandParameters);
        } catch (final Exception e) {
            DiscordBot.getLogger().error(e);
        }
    }

    private void sendIncorrectCommandHelpMessage(@NonNull final CommandParameters commandParameters, @NonNull final List<AbstractCommand<?>> similarCommands, @NonNull final String firstArg) {
        final StringBuilder description = new StringBuilder();
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        if (similarCommands.isEmpty()) {
            description.append(MarkdownUtil.monospace(firstArg)).append(" is not a valid command.\n")
                    .append("Use the ").append(MarkdownUtil.bold(this.getModule().getModuleOrThrow(CommandModule.class).getMainCommand() + " help")).append(" command or click the ")
                    .append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all commands.");

        } else {
            description.append(MarkdownUtil.monospace(firstArg)).append(" is not a valid command.\n")
                    .append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarCommands.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();
                final AbstractCommand<?> similarCommand = similarCommands.get(index);

                description.append(emote).append(" ").append(MarkdownUtil.bold(similarCommand.getName())).append(" | ").append(similarCommand.getDescription()).append("\n");
                emotes.put(emote, new CommandEmoteReaction(similarCommand, commandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(" All commands");
        }
        this.getModule().getModuleOrThrow(CommandModule.class)
                .getCommand(HelpCommand.class)
                .ifPresent(helpCommand -> emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(helpCommand, commandParameters)));

        AbstractCommand.sendEmoteMessage(
                commandParameters,
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Command")
                        .setDescription(description)
                        .setFooter("↓ Click Me!"),
                emotes
        );
    }
}
