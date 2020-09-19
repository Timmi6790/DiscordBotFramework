package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.datatypes.builders.MapBuilder;
import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.GetModule;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.EnumUtilities;
import de.timmi6790.discord_framework.utilities.StringUtilities;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractCommand<T extends AbstractModule> extends GetModule<T> {
    private static final String ERROR = "Error";

    private static final int COMMAND_USER_RATE_LIMIT = 10;
    private static final String INSERT_COMMAND_LOG = "INSERT INTO command_log(command_id, command_cause_id, command_status_id, in_guild) VALUES(:commandId, " +
            "(SELECT id FROM command_cause WHERE cause_name = :causeName LIMIT 1), (SELECT id FROM command_status WHERE status_name = :statusName LIMIT 1), :inGuild);";

    private static final EnumSet<Permission> MINIMUM_DISCORD_PERMISSIONS = EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS);

    private static final Pattern DISCORD_USER_ID_PATTERN = Pattern.compile("^(<@[!&])?(\\d*)>?$");
    private static final Pattern DISCORD_USER_TAG_PATTERN = Pattern.compile("^(.{2,32})#(\\d{4})$");
    private final String name;
    private final String syntax;
    private final String[] aliasNames;
    private Map<Class<? extends CommandProperty<?>>, CommandProperty<?>> propertiesMap = new HashMap<>();
    private int dbId = -1;
    private Class<? extends AbstractModule> commandModule;
    private int permissionId = -1;
    private String category;
    private String description;

    public AbstractCommand(@NonNull final String name, @NonNull final String category, @NonNull final String description, @NonNull final String syntax, final String... aliasNames) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.syntax = syntax;
        this.aliasNames = aliasNames;
    }

    protected static boolean hasRequiredDiscordPerms(@NonNull final CommandParameters commandParameters, @NonNull final EnumSet<Permission> requiredPermissions) {
        if (commandParameters.isGuildCommand()) {
            final EnumSet<Permission> wantedDiscordPerms = EnumSet.copyOf(MINIMUM_DISCORD_PERMISSIONS);
            wantedDiscordPerms.addAll(requiredPermissions);

            final Set<Permission> permissions = commandParameters.getDiscordPermissions();
            wantedDiscordPerms.removeIf(permissions::contains);

            if (!wantedDiscordPerms.isEmpty()) {
                final String perms = wantedDiscordPerms.stream()
                        .map(perm -> MarkdownUtil.monospace(perm.getName()))
                        .collect(Collectors.joining(","));

                DiscordMessagesUtilities.sendPrivateMessage(
                        commandParameters.getUser(),
                        DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                                .setTitle("Missing Permission")
                                .setDescription("The bot is missing " + perms + " permission(s).")
                );

                return false;
            }
        }

        return true;
    }

    protected static boolean isUserBanned(@NonNull final CommandParameters commandParameters) {
        if (commandParameters.getUserDb().isBanned()) {
            DiscordMessagesUtilities.sendPrivateMessage(
                    commandParameters.getUser(),
                    DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                            .setTitle("You are banned")
                            .setDescription("You are banned from using this bot.")
            );
            return true;
        }
        return false;
    }

    protected static boolean isServerBanned(@NonNull final CommandParameters commandParameters) {
        if (commandParameters.getChannelDb().getGuildDb().isBanned()) {
            DiscordMessagesUtilities.sendMessageTimed(
                    commandParameters.getLowestMessageChannel(),
                    DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                            .setTitle("Banned Server")
                            .setDescription("This server is banned from using this bot."),
                    90
            );

            return true;
        }
        return false;
    }

    protected static MultiEmbedBuilder getEmbedBuilder(@NonNull final CommandParameters commandParameters) {
        return DiscordMessagesUtilities.getEmbedBuilder(commandParameters);
    }

    protected static void sendMissingPermissionMessage(@NonNull final CommandParameters commandParameters) {
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Missing perms")
                        .setDescription("You don't have the permissions to run this command."),
                90
        );
    }

    protected static void sendEmoteMessage(@NonNull final CommandParameters commandParameters, @NonNull final MultiEmbedBuilder embedBuilder, @NonNull final Map<String, AbstractEmoteReaction> emotes) {
        commandParameters.getLowestMessageChannel().sendMessage(embedBuilder.setFooter("↓ Click Me!").buildSingle())
                .queue(message -> {
                    if (!emotes.isEmpty()) {
                        final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, commandParameters.getUser().getIdLong(),
                                commandParameters.getLowestMessageChannel().getIdLong());
                        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(EmoteReactionModule.class).addEmoteReactionMessage(message, emoteReactionMessage);
                    }

                    message.delete().queueAfter(90, TimeUnit.SECONDS, null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                });
    }

    protected static void sendTimedMessage(@NonNull final CommandParameters commandParameters, @NonNull final MultiEmbedBuilder embedBuilder, final int deleteTime) {
        DiscordMessagesUtilities.sendMessageTimed(commandParameters.getLowestMessageChannel(), embedBuilder, deleteTime);
    }

    protected static void sendMessage(@NonNull final CommandParameters commandParameters, @NonNull final MultiEmbedBuilder embedBuilder) {
        DiscordMessagesUtilities.sendMessage(commandParameters.getLowestMessageChannel(), embedBuilder);
    }

    protected static void sendMessage(@NonNull final CommandParameters commandParameters, @NonNull final MultiEmbedBuilder embedBuilder, @NonNull final Consumer<Message> success) {
        DiscordMessagesUtilities.sendMessage(commandParameters.getLowestMessageChannel(), embedBuilder, success);
    }

    protected static void throwInvalidArg(@NonNull final CommandParameters commandParameters, final int argPos, @NonNull final String argName) {
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid " + argName)
                        .setDescription(MarkdownUtil.monospace(commandParameters.getArgs()[argPos]) + " is not a valid " + MarkdownUtil.bold(argName.toLowerCase()) + "."),
                120
        );

        throw new CommandReturnException(CommandResult.INVALID_ARGS);
    }

    protected static void sendEmoteMessage(@NonNull final CommandParameters commandParameters, @NonNull final String title, @NonNull final String description,
                                           @NonNull final Map<String, AbstractEmoteReaction> emotes) {
        sendEmoteMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle(title)
                        .setDescription(description),
                emotes
        );
    }

    private void logCommand(final CommandParameters commandParameters, final CommandResult commandResult) {
        this.getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(INSERT_COMMAND_LOG)
                        .bind("commandId", this.dbId)
                        .bind("causeName", commandParameters.getCommandCause().name().toLowerCase())
                        .bind("statusName", commandResult.name().toLowerCase())
                        .bind("inGuild", commandParameters.isGuildCommand())
                        .execute()
        );
    }

    private CommandResult executeSave(final CommandParameters commandParameters) {
        try {
            return this.onCommand(commandParameters);
        } catch (final CommandReturnException e) {
            e.getEmbedBuilder().ifPresent(embedBuilder -> sendTimedMessage(commandParameters, embedBuilder, 90));
            return e.getCommandResult();

        } catch (final Exception e) {
            DiscordBot.getLogger().error(e);
            this.sendErrorMessage(commandParameters, "Unknown");

            // Sentry error
            final Map<String, String> data = MapBuilder.<String, String>ofHashMap(4)
                    .put("channelId", String.valueOf(commandParameters.getChannelDb().getDatabaseId()))
                    .put("userId", String.valueOf(commandParameters.getUserDb().getDatabaseId()))
                    .put("args", Arrays.toString(commandParameters.getArgs()))
                    .put("command", this.name)
                    .build();

            final Breadcrumb breadcrumb = new BreadcrumbBuilder()
                    .setCategory("Command")
                    .setData(data)
                    .build();

            final EventBuilder eventBuilder = new EventBuilder()
                    .withMessage("Command Exception")
                    .withLevel(Event.Level.ERROR)
                    .withBreadcrumbs(Collections.singletonList(breadcrumb))
                    .withLogger(AbstractCommand.class.getName())
                    .withSentryInterface(new ExceptionInterface(e));

            if (this.getModule().getSentry() != null) {
                this.getModule().getSentry().sendEvent(eventBuilder);
            }

            return CommandResult.ERROR;
        }
    }

    protected boolean customPermissionCheck(final CommandParameters commandParameters) {
        return true;
    }

    protected abstract CommandResult onCommand(CommandParameters commandParameters);

    public boolean hasPermission(final CommandParameters commandParameters) {
        // Properties Check
        for (final CommandProperty<?> commandProperty : this.propertiesMap.values()) {
            if (!commandProperty.onPermissionCheck(this, commandParameters)) {
                return false;
            }
        }

        // Permission check
        if (this.permissionId != -1 && !commandParameters.getUserDb().getAllPermissionIds().contains(this.permissionId)) {
            return false;
        }

        return this.customPermissionCheck(commandParameters);
    }

    protected void addProperty(final CommandProperty<?> property) {
        this.propertiesMap.put((Class<? extends CommandProperty<?>>) property.getClass(), property);
    }

    protected void addProperties(final CommandProperty<?>... properties) {
        Arrays.stream(properties).forEach(this::addProperty);
    }

    public <V> V getPropertyValueOrDefault(final Class<? extends CommandProperty<V>> propertyClass, final V defaultValue) {
        final CommandProperty<V> property = (CommandProperty<V>) this.propertiesMap.get(propertyClass);
        if (property != null) {
            return property.getValue();
        }
        return defaultValue;
    }

    protected void setPermission(final String permission) {
        this.permissionId = this.getModuleManager()
                .getModuleOrThrow(PermissionsModule.class)
                .addPermission(permission);
    }

    public void runCommand(final CommandParameters commandParameters) {
        if (this.getModule().getModuleOrThrow(CommandModule.class).getCommandSpamCache().get(commandParameters.getUserDb().getDiscordId()).get() > COMMAND_USER_RATE_LIMIT) {
            return;
        }

        // Ban checks
        if (isUserBanned(commandParameters) || isServerBanned(commandParameters)) {
            return;
        }

        final EnumSet<Permission> requiredDiscordPerms = this.getPropertyValueOrDefault(RequiredDiscordBotPermsCommandProperty.class, EnumSet.noneOf(Permission.class));
        final boolean hasDiscordPerms = hasRequiredDiscordPerms(commandParameters, requiredDiscordPerms);
        if (!hasDiscordPerms) {
            return;
        }

        if (!this.hasPermission(commandParameters)) {
            sendMissingPermissionMessage(commandParameters);
            this.logCommand(commandParameters, CommandResult.NO_PERMS);
            return;
        }

        // Property checks
        for (final CommandProperty<?> commandProperty : this.propertiesMap.values()) {
            if (!commandProperty.onCommandExecution(this, commandParameters)) {
                // TODO: Add proper result logging
                //            this.logCommand(commandParameters, CommandResult.MISSING_ARGS);
                return;
            }
        }

        // Command pre event
        final Optional<EventModule> eventModuleOpt = this.getModuleManager().getModule(EventModule.class);
        eventModuleOpt.ifPresent(eventModule -> {
            final CommandExecutionEvent.Pre commandExecutionPre = new CommandExecutionEvent.Pre(this, commandParameters);
            eventModule.executeEvent(commandExecutionPre);
        });

        // Run command
        this.getModule().getModuleOrThrow(CommandModule.class).getCommandSpamCache().get(commandParameters.getUserDb().getDiscordId()).incrementAndGet();
        final CommandResult commandResult = this.executeSave(commandParameters);
        this.logCommand(commandParameters, commandResult == null ? CommandResult.MISSING : commandResult);

        // Command post event
        eventModuleOpt.ifPresent(eventModule -> {
            final CommandExecutionEvent.Post commandExecutionPost = new CommandExecutionEvent.Post(this, commandParameters, commandResult);
            eventModule.executeEvent(commandExecutionPost);
        });
    }

    public String[] getAliasNames() {
        return this.aliasNames.clone();
    }

    // Old Stuff
    public List<String> getFormattedExampleCommands() {
        final String mainCommand = this.getModuleManager().getModuleOrThrow(CommandModule.class).getMainCommand();
        return Arrays.stream(this.getPropertyValueOrDefault(ExampleCommandsCommandProperty.class, new String[0]))
                .map(exampleCommand -> mainCommand + " " + this.name + " " + exampleCommand)
                .collect(Collectors.toList());
    }

    public void sendMissingArgsMessage(final CommandParameters commandParameters) {
        this.sendMissingArgsMessage(commandParameters, this.getPropertyValueOrDefault(MinArgCommandProperty.class, 0));
    }

    protected void sendMissingArgsMessage(final CommandParameters commandParameters, final int requiredSyntaxLenght) {
        final String[] args = commandParameters.getArgs();
        final String[] splitSyntax = this.syntax.split(" ");

        final StringJoiner requiredSyntax = new StringJoiner(" ");
        for (int index = 0; Math.min(requiredSyntaxLenght, splitSyntax.length) > index; index++) {
            requiredSyntax.add(args.length > index ? args[index] : MarkdownUtil.bold(splitSyntax[index]));
        }

        final String exampleCommands = String.join("\n", this.getFormattedExampleCommands());
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters).setTitle("Missing Args")
                        .setDescription("You are missing a few required arguments.\nIt is required that you enter the bold arguments.")
                        .addField("Required Syntax", requiredSyntax.toString(), false)
                        .addField("Command Syntax", this.getSyntax(), false)
                        .addField("Example Commands", exampleCommands, false, !exampleCommands.isEmpty()),
                90
        );
    }

    protected void sendErrorMessage(final CommandParameters commandParameters, final String error) {
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters).setTitle("Something went wrong")
                        .setDescription("Something went wrong while executing this command.")
                        .addField("Command", this.getName(), false)
                        .addField("Args", String.join(" ", commandParameters.getArgs()), false)
                        .addField(ERROR, error, false),
                90
        );
    }

    protected void sendHelpMessage(final CommandParameters commandParameters, final String userArg, final int argPos, final String argName,
                                   final AbstractCommand<?> command, final String[] newArgs, final List<String> similarNames) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder helpDescription = new StringBuilder();
        helpDescription.append(MarkdownUtil.monospace(userArg)).append(" is not a valid ").append(argName).append(".\n");

        if (similarNames.isEmpty() && command != null) {
            helpDescription.append("Use the ").append(MarkdownUtil.bold(this.getModuleManager().getModuleOrThrow(CommandModule.class).getMainCommand() + " " + command.getName() + " " + String.join(" ", newArgs)))
                    .append(" command or click the ").append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all ").append(argName).append("s.");

        } else {
            helpDescription.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarNames.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                helpDescription.append(emote).append(" ").append(MarkdownUtil.bold(similarNames.get(index))).append("\n");

                final String[] newArgsParameter = commandParameters.getArgs();
                newArgsParameter[argPos] = similarNames.get(index);
                final CommandParameters newCommandParameters = new CommandParameters(commandParameters, newArgsParameter);
                emotes.put(emote, new CommandEmoteReaction(this, newCommandParameters));
            }

            if (command != null) {
                helpDescription.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(MarkdownUtil.bold("All " + argName + "s"));
            }
        }

        if (command != null) {
            final CommandParameters newCommandParameters = new CommandParameters(commandParameters, newArgs);
            emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(command, newCommandParameters));
        }

        sendEmoteMessage(commandParameters, "Invalid " + StringUtilities.capitalize(argName), helpDescription.toString(), emotes);
    }

    // Checks
    protected void checkArgLength(final CommandParameters commandParameters, final int length) {
        if (length > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, Math.max(this.getPropertyValueOrDefault(MinArgCommandProperty.class, length), length));
            throw new CommandReturnException(CommandResult.MISSING_ARGS);
        }
    }


    // Arg parser
    protected String getFromListIgnoreCaseThrow(final CommandParameters commandParameters, final int argPos, final List<String> possibleArguments) {
        final String userArg = commandParameters.getArgs()[argPos];
        final Optional<String> arg = possibleArguments.stream()
                .filter(possibleArg -> possibleArg.equalsIgnoreCase(userArg))
                .findAny();

        if (arg.isPresent()) {
            return arg.get();
        }

        AbstractCommand.this.sendHelpMessage(commandParameters, userArg, argPos, "argument", null, null, possibleArguments);
        throw new CommandReturnException();
    }

    protected <E extends Enum> E getFromEnumIgnoreCaseThrow(final CommandParameters commandParameters, final int argPos, final E[] enumValue) {
        final String userArg = commandParameters.getArgs()[argPos];
        final Optional<E> arg = EnumUtilities.getIgnoreCase(userArg, enumValue);
        if (arg.isPresent()) {
            return arg.get();
        }

        this.sendHelpMessage(commandParameters, userArg, argPos, "argument", null, null, EnumUtilities.getPrettyNames(enumValue));
        throw new CommandReturnException();
    }

    protected AbstractCommand<? extends AbstractModule> getCommandThrow(final CommandParameters commandParameters, final int argPos) {
        final String commandName = commandParameters.getArgs()[argPos];
        final Optional<AbstractCommand<?>> command = this.getModuleManager().getModuleOrThrow(CommandModule.class).getCommand(commandName);
        if (command.isPresent()) {
            return command.get();
        }

        final List<AbstractCommand<?>> similarCommands = this.getModuleManager().getModuleOrThrow(CommandModule.class).getSimilarCommands(commandParameters, commandName, 0.6, 3);
        if (!similarCommands.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarCommands.get(0);
        }

        AbstractCommand.this.sendHelpMessage(
                commandParameters,
                commandName,
                argPos,
                "command",
                this.getModuleManager().getModuleOrThrow(CommandModule.class).getCommand(HelpCommand.class).orElse(null),
                new String[0],
                similarCommands.stream().map(AbstractCommand::getName).collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected Rank getRankThrow(final CommandParameters commandParameters, final int position) {
        final String userInput = commandParameters.getArgs()[position];

        return this.getModuleManager().getModuleOrThrow(RankModule.class).getRanks()
                .stream()
                .filter(rank -> rank.getName().equalsIgnoreCase(userInput))
                .findAny()
                .orElseThrow(() -> new CommandReturnException(
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR)
                                .setDescription(MarkdownUtil.monospace(userInput) + " is not a valid rank.")
                ));
    }

    public int getPermissionIdThrow(final CommandParameters commandParameters, final int argPos) {
        final String permArg = commandParameters.getArgs()[argPos];
        final Optional<AbstractCommand<?>> commandOpt = this.getModuleManager().getModuleOrThrow(CommandModule.class).getCommand(permArg);

        if (commandOpt.isPresent()) {
            final AbstractCommand<?> command = commandOpt.get();
            if (command.getPermissionId() == -1) {
                throw new CommandReturnException(
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR)
                                .setDescription(MarkdownUtil.monospace(command.getName()) + " command has no permission.")
                );
            }

            return command.getPermissionId();
        } else {
            return this.getModuleManager().getModuleOrThrow(PermissionsModule.class).getPermissionId(permArg)
                    .orElseThrow(() -> new CommandReturnException(
                            getEmbedBuilder(commandParameters)
                                    .setTitle(ERROR)
                                    .setDescription(MarkdownUtil.monospace(permArg) + " is not a valid permission.")
                    ));
        }
    }

    protected User getDiscordUserThrow(final CommandParameters commandParameters, final int argPos) {
        final String discordUserName = commandParameters.getArgs()[argPos];
        final Matcher userIdMatcher = DISCORD_USER_ID_PATTERN.matcher(discordUserName);
        if (userIdMatcher.find()) {
            final User user = UserDb.getUserCache().get(Long.valueOf(userIdMatcher.group(2)));
            if (user != null) {
                return user;
            }
        }

        if (DISCORD_USER_TAG_PATTERN.matcher(discordUserName).find()) {
            final User user = this.getModule().getDiscord().getUserByTag(discordUserName);
            if (user != null) {
                return user;
            }
        }

        throw new CommandReturnException(
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid User")
                        .setDescription(MarkdownUtil.monospace(discordUserName) + " is not a valid discord user.")
        );
    }
}
