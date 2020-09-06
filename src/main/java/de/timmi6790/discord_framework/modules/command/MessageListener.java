package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.GetModule;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener extends GetModule<CommandModule> {
    private static final Pattern FIRST_ARG_PATTERN = Pattern.compile("^([^\\s]+) ?(.*)");

    private Optional<String> getParsedStart(final String rawMessage, final GuildDb guildDb) {
        // Check if the message matches the main or guild specific start regex

        final Matcher mainMatcher = this.getModule().getMainCommandPattern().matcher(rawMessage);
        if (mainMatcher.find()) {
            return Optional.of(mainMatcher.group(1).trim());

        }

        final Optional<Pattern> commandAliasPattern = guildDb.getCommandAliasPattern();
        if (commandAliasPattern.isPresent()) {
            final Matcher guildAliasMatcher = commandAliasPattern.get().matcher(rawMessage);
            if (guildAliasMatcher.find()) {
                return Optional.of(guildAliasMatcher.group(1).trim());
            }
        }

        return Optional.empty();
    }

    @SubscribeEvent
    public void onTextMessage(final MessageReceivedEvent event) {
        // Ignore yourself
        if (this.getModule().getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        final long guildId = event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0;
        final GuildDb guildDb = this.getModuleManager().getModuleOrThrow(GuildDbModule.class).getOrCreate(guildId);

        final String rawMessage = event.getMessage().getContentRaw().replace("\n", " ");
        final Optional<String> parsedStart = this.getParsedStart(rawMessage, guildDb);

        // Invalid start
        if (!parsedStart.isPresent()) {
            return;
        }

        final Matcher commandMatcher = FIRST_ARG_PATTERN.matcher(parsedStart.get());
        final Optional<AbstractCommand<?>> commandOpt;
        final String commandName;
        final String rawArgs;
        if (commandMatcher.find()) {
            commandName = commandMatcher.group(1);
            commandOpt = this.getModule().getCommand(commandName);
            rawArgs = commandMatcher.group(2);
        } else {
            commandName = null;
            commandOpt = this.getModule().getCommand(HelpCommand.class);
            rawArgs = parsedStart.get();
        }

        final CommandParameters commandParameters = new CommandParameters(event, rawArgs);
        final AbstractCommand<?> command;

        if (commandOpt.isPresent()) {
            command = commandOpt.get();
        } else {
            final List<AbstractCommand<?>> similarCommands = this.getModule().getSimilarCommands(commandParameters, commandName, 0.6, 3);
            if (!similarCommands.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
                command = similarCommands.get(0);

            } else {
                this.sendIncorrectCommandHelpMessage(event, similarCommands, commandName, commandParameters);
                return;
            }
        }

        try {
            command.runCommand(commandParameters);
        } catch (final Exception e) {
            DiscordBot.getLogger().error(e);
        }
    }

    private void sendIncorrectCommandHelpMessage(final MessageReceivedEvent event, final List<AbstractCommand<?>> similarCommands, final String firstArg, final CommandParameters commandParameters) {
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
        this.getModule().getModuleOrThrow(CommandModule.class).getCommand(HelpCommand.class).ifPresent(helpCommand -> emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(helpCommand, commandParameters)));

        final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, event.getAuthor().getIdLong(), event.getChannel().getIdLong());

        event.getChannel().sendMessage(
                DiscordMessagesUtilities.getEmbedBuilder(event.getAuthor(), event.getMember())
                        .setTitle("Invalid Command")
                        .setDescription(description)
                        .setFooter("↓ Click Me!")
                        .buildSingle()
        ).queue(sendMessage -> {
            this.getModule()
                    .getModuleOrThrow(EmoteReactionModule.class)
                    .addEmoteReactionMessage(sendMessage, emoteReactionMessage);

            sendMessage
                    .delete()
                    .queueAfter(
                            90,
                            TimeUnit.SECONDS,
                            null,
                            new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)
                    );
        });
    }
}
