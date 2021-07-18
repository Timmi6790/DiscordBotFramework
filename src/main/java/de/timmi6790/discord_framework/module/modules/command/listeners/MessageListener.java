package de.timmi6790.discord_framework.module.modules.command.listeners;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandCause;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.CommandButtonAction;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener {
    private static final String BASE_COMMAND_PATTERN = "^(?:(?:%s)|(?:<@[!&]%s>))\\s*([\\S]*)\\s?([\\s\\S]*)$";

    private final Pattern commandPattern;

    private final CommandModule commandModule;
    private final UserDbModule userDbModule;
    private final ChannelDbModule channelDbModule;
    private final ButtonReactionModule buttonReactionModule;
    private final Command helpCommand;

    private final long botId;

    public MessageListener(final CommandModule commandModule,
                           final UserDbModule userDbModule,
                           final ChannelDbModule channelDbModule,
                           final ButtonReactionModule buttonReactionModule,
                           final Command helpCommand) {
        this.commandModule = commandModule;
        this.userDbModule = userDbModule;
        this.channelDbModule = channelDbModule;
        this.buttonReactionModule = buttonReactionModule;
        this.helpCommand = helpCommand;
        this.botId = commandModule.getBotId();

        final String constructedCommandPattern = String.format(
                BASE_COMMAND_PATTERN,
                commandModule.getMainCommand().trim(),
                this.botId
        );
        this.commandPattern = Pattern.compile(constructedCommandPattern, Pattern.CASE_INSENSITIVE);
    }

    private boolean hasMessageWritePermission(final CommandParameters commandParameters) {
        return commandParameters.getDiscordPermissions().contains(Permission.MESSAGE_WRITE);
    }

    private void sendHelpMessage(final CommandParameters commandParameters,
                                 final String commandName,
                                 final List<Command> similarCommands) {
        final Map<Button, ButtonAction> buttons = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder(
                String.format(
                        "%s is not a valid %s.%n",
                        MarkdownUtil.monospace(commandName),
                        "command"
                )
        ).append("Is it possible that you wanted to write?\n\n");

        final int allowedButtons = 4;
        for (int index = 0; Math.min(allowedButtons, similarCommands.size()) > index; index++) {
            final Command similarValue = similarCommands.get(index);
            final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

            description.append(String.format(
                    "%s %s%n",
                    emote,
                    similarValue.getName()
            ));

            buttons.put(
                    Button.of(ButtonStyle.SECONDARY, emote, "").withEmoji(Emoji.fromUnicode(emote)),
                    new CommandButtonAction(
                            similarValue.getClass(),
                            commandParameters
                    )
            );
        }

        description.append(String.format(
                "%n%s %s",
                DiscordEmotes.FOLDER.getEmote(),
                MarkdownUtil.bold("All commands")
        ));

        final CommandParameters newCommandParameters = CommandParameters.of(
                "",
                commandParameters.isGuildCommand(),
                commandParameters.getCommandCause(),
                this.commandModule,
                commandParameters.getChannelDb(),
                commandParameters.getUserDb()
        );
        final String everythingEmote = DiscordEmotes.FOLDER.getEmote();
        buttons.put(
                Button.of(ButtonStyle.SECONDARY, everythingEmote, "")
                        .withEmoji(Emoji.fromUnicode(everythingEmote)),
                new CommandButtonAction(
                        this.helpCommand.getClass(),
                        newCommandParameters
                )
        );

        // Send message
        final MessageChannel channel;
        if (this.hasMessageWritePermission(commandParameters)) {
            channel = commandParameters.getLowestMessageChannel();
        } else {
            channel = commandParameters.getUserTextChannel();
        }
        channel.sendMessageEmbeds(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid Command")
                        .setDescription(description.toString())
                        .setFooter("↓ Click Me!")
                        .build()
        )
                .setActionRows(ActionRow.of(buttons.keySet()))
                .queue(message ->
                        this.buttonReactionModule.addButtonReactionMessage(
                                message,
                                new ButtonReaction(
                                        buttons,
                                        commandParameters.getUserDb().getDiscordId()
                                )
                        )
                );
    }

    private Optional<Command> getCommand(final String commandName, final CommandParameters commandParameters) {
        if (commandName.isEmpty()) {
            return Optional.of(this.helpCommand);
        }

        final Optional<Command> commandOpt = this.commandModule.getCommand(commandName);
        if (commandOpt.isPresent()) {
            return commandOpt;
        }

        final List<Command> similarCommands = DataUtilities.getSimilarityList(
                commandName,
                this.commandModule.getCommands(command -> command.canExecute(commandParameters)),
                Command::getName,
                0.6,
                5
        );

        if (similarCommands.isEmpty()) {
            final MultiEmbedBuilder embedBuilder = commandParameters.getEmbedBuilder()
                    .setTitle("Can't find a valid command")
                    .setDescription(
                            "Your input %s is not similar with one of the valid commands." +
                                    "Use the %s command to see all valid commands.",
                            MarkdownUtil.monospace(commandName),
                            MarkdownUtil.monospace(this.commandModule.getMainCommand() + this.helpCommand.getName())
                    );
            if (this.hasMessageWritePermission(commandParameters)) {
                commandParameters.sendMessage(embedBuilder);
            } else {
                commandParameters.sendPrivateMessage(embedBuilder);
            }

            return Optional.empty();
        }

        // Handle auto correction
        if (commandParameters.getUserDb().hasAutoCorrection()) {
            return Optional.of(similarCommands.get(0));
        }

        // Send help message
        this.sendHelpMessage(commandParameters, commandName, similarCommands);

        return Optional.empty();
    }

    @SneakyThrows
    @SubscribeEvent
    public void onTextMessage(final MessageReceivedEvent event) {
        // Ignore yourself
        if (this.botId == event.getAuthor().getIdLong()) {
            return;
        }

        // Check if the message either starts with the main command or if the bot was tagged
        final Matcher commandMatcher = this.commandPattern.matcher(event.getMessage().getContentRaw());
        if (!commandMatcher.find()) {
            return;
        }

        // Add user to cache
        final User author = event.getAuthor();
        this.userDbModule.addUserToCache(author);

        // Get repository objects async
        final CompletableFuture<UserDb> userDbFuture = CompletableFuture.supplyAsync(() ->
                this.userDbModule.getOrCreate(author.getIdLong())
        );

        final CompletableFuture<ChannelDb> channelDbFuture = CompletableFuture.supplyAsync(() -> {
                    if (event.isFromGuild()) {
                        return this.channelDbModule.getOrCreate(
                                event.getChannel().getIdLong(),
                                event.getGuild().getIdLong()
                        );
                    } else {
                        return this.channelDbModule.getOrCreatePrivateMessage(
                                event.getChannel().getIdLong()
                        );
                    }
                }
        );

        final String commandName = commandMatcher.group(1);
        final String rawArguments = commandMatcher.group(2);
        final CommandParameters commandParameters = CommandParameters.of(
                rawArguments,
                event.isFromGuild(),
                BaseCommandCause.MESSAGE,
                this.commandModule,
                channelDbFuture.get(),
                userDbFuture.get()
        );

        this.getCommand(commandName, commandParameters)
                .ifPresent(command -> command.executeCommand(commandParameters));
    }
}
