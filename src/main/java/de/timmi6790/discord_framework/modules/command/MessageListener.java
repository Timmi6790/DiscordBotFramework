package de.timmi6790.discord_framework.modules.command;

import com.google.common.base.Splitter;
import de.timmi6790.discord_framework.modules.GetModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class MessageListener extends GetModule<CommandModule> {
    private static final Splitter SPLITTER = Splitter.on(' ')
            .trimResults()
            .omitEmptyStrings();

    @SubscribeEvent
    public void onTextMessage(final MessageReceivedEvent event) {
        // Ignore yourself
        if (this.getModule().getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        final long guildId = event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0;
        final GuildDb guildDb = this.getModuleManager().getModuleOrThrow(GuildDbModule.class).getOrCreate(guildId);

        String rawMessage = event.getMessage().getContentRaw().replace("\n", " ");
        boolean validStart = false;

        // Check if the message matches the main or guild specific start regex
        final Matcher mainMatcher = this.getModule().getMainCommandPattern().matcher(rawMessage);
        if (mainMatcher.find()) {
            validStart = true;
            rawMessage = mainMatcher.group(1).trim();

        } else if (guildDb.getCommandAliasPattern().isPresent()) {
            final Matcher guildAliasMatcher = guildDb.getCommandAliasPattern().get().matcher(rawMessage);
            if (guildAliasMatcher.find()) {
                validStart = true;
                rawMessage = guildAliasMatcher.group(1).trim();
            }
        }

        // Invalid start
        if (!validStart) {
            return;
        }

        final UserDb userDb = this.getModule().getModuleOrThrow(UserDbModule.class).getOrCreate(event.getAuthor().getIdLong());

        final String[] args = SPLITTER.splitToList(rawMessage).toArray(new String[0]);
        final CommandParameters commandParameters = new CommandParameters(
                rawMessage,
                args.length == 0 ? args : Arrays.copyOfRange(args, 1, args.length),
                event.isFromGuild(),
                CommandCause.USER,
                this.getModule().getModuleOrThrow(ChannelDbModule.class).getOrCreate(event.getChannel().getIdLong(), guildDb.getDiscordId()),
                userDb
        );
        UserDb.getUserCache().put(event.getAuthor().getIdLong(), event.getAuthor());

        final Optional<AbstractCommand<?>> commandOpt = args.length == 0 ? this.getModule().getCommand(HelpCommand.class) : this.getModule().getCommand(args[0]);
        final AbstractCommand<?> command;
        if (commandOpt.isPresent()) {
            command = commandOpt.get();
        } else {
            final List<AbstractCommand<?>> similarCommands = this.getModule().getSimilarCommands(commandParameters, args[0], 0.6, 3);
            if (!similarCommands.isEmpty() && userDb.hasAutoCorrection()) {
                command = similarCommands.get(0);

            } else {
                this.sendIncorrectCommandHelpMessage(event, similarCommands, args[0], commandParameters);
                return;
            }
        }

        command.runCommand(commandParameters);
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
                DiscordMessagesUtilities.getEmbedBuilder(event.getAuthor(), Optional.ofNullable(event.getMember()))
                        .setTitle("Invalid Command")
                        .setDescription(description)
                        .setFooter("↓ Click Me!")
                        .build())
                .queue(sendMessage -> {
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
                        }
                );
    }
}
