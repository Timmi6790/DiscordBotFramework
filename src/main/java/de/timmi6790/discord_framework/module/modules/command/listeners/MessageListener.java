package de.timmi6790.discord_framework.module.modules.command.listeners;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandCause;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.List;
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
    private final Command helpCommand;

    private final long botId;

    public MessageListener(final CommandModule commandModule,
                           final UserDbModule userDbModule,
                           final ChannelDbModule channelDbModule,
                           final Command helpCommand) {
        this.commandModule = commandModule;
        this.userDbModule = userDbModule;
        this.channelDbModule = channelDbModule;
        this.helpCommand = helpCommand;
        this.botId = commandModule.getBotId();

        final String constructedCommandPattern = String.format(
                BASE_COMMAND_PATTERN,
                commandModule.getMainCommand().trim(),
                this.botId
        );
        this.commandPattern = Pattern.compile(constructedCommandPattern);
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
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Can't find a valid command")
                            .setDescription(
                                    "Your input %s is not similar with one of the valid commands." +
                                            "Use the %s command to see all valid commands.",
                                    MarkdownUtil.monospace(commandName),
                                    MarkdownUtil.monospace(this.commandModule.getMainCommand() + this.helpCommand.getName())
                            )
            );

            return Optional.empty();
        }

        // Handle auto correction
        if (commandParameters.getUserDb().hasAutoCorrection()) {
            return Optional.of(similarCommands.get(0));
        }

        // TODO: Send help thing
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Can't find a valid command")
                        .setDescription("TODO: Shown help menu")
        );

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

        // Get repository objects async
        final CompletableFuture<UserDb> userDbFuture = CompletableFuture.supplyAsync(() ->
                this.userDbModule.getOrCreate(event.getAuthor().getIdLong())
        );
        final CompletableFuture<ChannelDb> channelDbFuture = CompletableFuture.supplyAsync(() ->
                this.channelDbModule.getOrCreate(
                        event.getChannel().getIdLong(),
                        event.getGuild().getIdLong()
                )
        );

        final String commandName = commandMatcher.group(1);
        final String rawArguments = commandMatcher.group(2);

        // TODO: Don't forget to add the user and channel to the cache objects to prevent a further rest request
        final CommandParameters commandParameters = CommandParameters.of(
                rawArguments,
                event.isFromGuild(),
                BaseCommandCause.MESSAGE,
                channelDbFuture.get(),
                userDbFuture.get()
        );

        this.getCommand(commandName, commandParameters)
                .ifPresent(command -> command.executeCommand(commandParameters));
    }
}
