package de.timmi6790.discord_framework.module.modules.command;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.module.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.reactions.emote.actions.CommandEmoteAction;
import de.timmi6790.discord_framework.module.modules.reactions.emote.actions.EmoteAction;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.commons.EnumUtilities;
import de.timmi6790.discord_framework.utilities.commons.StringUtilities;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import de.timmi6790.discord_framework.utilities.sentry.BreadcrumbBuilder;
import de.timmi6790.discord_framework.utilities.sentry.SentryEventBuilder;
import io.github.bucket4j.Bucket;
import io.prometheus.client.Histogram;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(exclude = {
        "commandModule",
        "permissionsModule",
        "eventModule",
        "eventModule",
        "rankModule",
        "discord"
})
@Log4j2
public abstract class AbstractCommand {
    private static final String COMMAND = "command";

    static final Histogram COMMAND_EXECUTION_TIME_FULL = Histogram.build()
            .name("command_execution_full_seconds")
            .labelNames(COMMAND)
            .help("Full command execution time in seconds. This includes all pre checks.")
            .register();
    static final Histogram COMMAND_EXECUTION_TIME = Histogram.build()
            .name("command_execution_seconds")
            .labelNames(COMMAND)
            .help("Command execution time in seconds.")
            .register();
    protected static final EnumSet<Permission> MINIMUM_DISCORD_PERMISSIONS = EnumSet.of(
            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_EMBED_LINKS
    );
    private static final String ERROR = "Error";
    private static final Pattern DISCORD_USER_ID_PATTERN = Pattern.compile("^(<@[!&])?(\\d*)>?$");

    // Modules
    private final CommandModule commandModule = this.getModuleOrThrow(CommandModule.class);
    private final PermissionsModule permissionsModule = this.getModuleOrThrow(PermissionsModule.class);
    private final EventModule eventModule = this.getModuleOrThrow(EventModule.class);
    private final RankModule rankModule = this.getModuleOrThrow(RankModule.class);
    private final ShardManager discord = this.getDiscordBot().getDiscord();

    // Command specific data
    private final String name;
    private final String syntax;
    private final String[] aliasNames;
    private Map<Class<? extends CommandProperty<?>>, CommandProperty<?>> propertiesMap = new HashMap<>();
    private int dbId = -1;
    private Class<? extends AbstractModule> registeredModule;
    private int permissionId = -1;
    private String category;
    private String description;

    protected AbstractCommand(@NonNull final String name,
                              @NonNull final String category,
                              @NonNull final String description,
                              @NonNull final String syntax,
                              final String... aliasNames) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.syntax = syntax;
        this.aliasNames = aliasNames.clone();
    }

    // Other
    public static boolean hasRequiredDiscordPerms(@NonNull final CommandParameters commandParameters,
                                                  @NonNull final Set<Permission> requiredPermissions) {
        // We only need to check the perms inside a guild.
        // We already have all permissions inside a user chat.
        if (commandParameters.isGuildCommand()) {
            final EnumSet<Permission> missingDiscordPerms = EnumSet.copyOf(MINIMUM_DISCORD_PERMISSIONS);
            missingDiscordPerms.addAll(requiredPermissions);

            final Set<Permission> permissions = commandParameters.getDiscordPermissions();
            missingDiscordPerms.removeIf(permissions::contains);

            // Send error message inside user dms
            if (!missingDiscordPerms.isEmpty()) {
                final StringJoiner missingPerms = new StringJoiner(",");
                for (final Permission permission : missingDiscordPerms) {
                    missingPerms.add(MarkdownUtil.monospace(permission.getName()));
                }
                final MultiEmbedBuilder embedBuilder = DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Missing Permission")
                        .setDescription(
                                "The bot is missing %s permission(s).",
                                MarkdownUtil.monospace(missingPerms.toString())
                        );

                // Only send it in the guild when we know that we have perms to do it
                if (missingDiscordPerms.contains(Permission.MESSAGE_WRITE)) {
                    DiscordMessagesUtilities.sendPrivateMessage(
                            commandParameters.getUser(),
                            embedBuilder
                    );
                } else {
                    DiscordMessagesUtilities.sendMessageTimed(
                            commandParameters.getLowestMessageChannel(),
                            embedBuilder,
                            300
                    );
                }
                return false;
            }
        }

        return true;
    }

    protected abstract CommandResult onCommand(CommandParameters commandParameters);

    protected final DiscordBot getDiscordBot() {
        return DiscordBot.getInstance();
    }

    protected ModuleManager getModuleManager() {
        return this.getDiscordBot().getModuleManager();
    }

    protected final <T extends AbstractModule> Optional<T> getModule(final Class<T> clazz) {
        return this.getModuleManager().getModule(clazz);
    }

    protected final <T extends AbstractModule> T getModuleOrThrow(final Class<T> clazz) {
        return this.getModuleManager().getModuleOrThrow(clazz);
    }

    protected MultiEmbedBuilder getEmbedBuilder(@NonNull final CommandParameters commandParameters) {
        return DiscordMessagesUtilities.getEmbedBuilder(commandParameters);
    }

    protected void sendMissingPermissionMessage(@NonNull final CommandParameters commandParameters) {
        this.sendTimedMessage(
                commandParameters,
                "Missing perms",
                "You don't have the permissions to run this command."
        );
    }

    protected void sendTimedMessage(final CommandParameters commandParameters,
                                    final String title,
                                    final String descriptionFormat,
                                    final Object... descriptionObjects) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(title)
                        .setDescription(
                                descriptionFormat,
                                descriptionObjects
                        )
        );
    }

    protected void sendTimedMessage(@NonNull final CommandParameters commandParameters,
                                    @NonNull final MultiEmbedBuilder embedBuilder) {
        // TODO: Calculate the delete time dynamic based on the size of the message
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                embedBuilder,
                90
        );
    }

    protected void sendTimedMessage(@NonNull final CommandParameters commandParameters,
                                    @NonNull final MultiEmbedBuilder embedBuilder,
                                    final int deleteTime) {
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                embedBuilder,
                deleteTime
        );
    }

    protected void sendMessage(@NonNull final CommandParameters commandParameters,
                               @NonNull final MultiEmbedBuilder embedBuilder) {
        DiscordMessagesUtilities.sendMessage(commandParameters.getLowestMessageChannel(), embedBuilder);
    }

    protected void sendMessage(@NonNull final CommandParameters commandParameters,
                               @NonNull final MultiEmbedBuilder embedBuilder,
                               @NonNull final Consumer<Message> success) {
        DiscordMessagesUtilities.sendMessage(commandParameters.getLowestMessageChannel(), embedBuilder, success);
    }

    protected void sendEmoteMessage(@NonNull final CommandParameters commandParameters,
                                    @NonNull final String title,
                                    @NonNull final String description,
                                    @NonNull final Map<String, EmoteAction> emotes) {
        this.sendEmoteMessage(
                commandParameters,
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle(title)
                        .setDescription(description),
                emotes
        );
    }

    protected void sendEmoteMessage(@NonNull final CommandParameters commandParameters,
                                    @NonNull final MultiEmbedBuilder embedBuilder,
                                    @NonNull final Map<String, EmoteAction> emotes) {
        DiscordMessagesUtilities.sendEmoteMessage(commandParameters, embedBuilder, emotes);
    }

    protected void throwInvalidArg(@NonNull final CommandParameters commandParameters,
                                   final int argPos,
                                   @NonNull final String argName) {
        this.sendTimedMessage(
                commandParameters,
                "Invalid " + argName,
                "%s is not a valid %s.",
                MarkdownUtil.monospace(commandParameters.getArgs()[argPos]),
                MarkdownUtil.bold(argName.toLowerCase())
        );

        throw new CommandReturnException(CommandResult.INVALID_ARGS);
    }

    protected boolean isUserBanned(@NonNull final CommandParameters commandParameters) {
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

    protected boolean isServerBanned(@NonNull final CommandParameters commandParameters) {
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

    public void runCommand(final @NonNull CommandParameters commandParameters) {
        final Histogram.Timer fullTimer = COMMAND_EXECUTION_TIME_FULL.labels(this.getName()).startTimer();
        try {

            final Bucket rateLimit = this.getCommandModule()
                    .resolveRateBucket(commandParameters.getUserDb().getDiscordId());
            if (!rateLimit.tryConsume(1)) {
                return;
            }

            // Ban checks
            if (this.isUserBanned(commandParameters) || this.isServerBanned(commandParameters)) {
                return;
            }

            // Discord perms check
            final EnumSet<Permission> requiredDiscordPerms = this.getPropertyValueOrDefault(
                    RequiredDiscordBotPermsCommandProperty.class,
                    EnumSet.noneOf(Permission.class)
            );
            if (!hasRequiredDiscordPerms(commandParameters, requiredDiscordPerms)) {
                return;
            }

            // Perm checks
            if (!this.hasPermission(commandParameters)) {
                this.sendMissingPermissionMessage(commandParameters);
                return;
            }

            // Property checks
            for (final CommandProperty<?> commandProperty : this.getPropertiesMap().values()) {
                if (!commandProperty.onCommandExecution(this, commandParameters)) {
                    return;
                }
            }

            // Command pre event
            this.getEventModule().executeEvent(new CommandExecutionEvent.Pre(this, commandParameters));

            // Run command
            CommandResult commandResult;
            try {
                // Track the time how long the command is executed
                commandResult = COMMAND_EXECUTION_TIME
                        .labels(this.getName())
                        .time(() -> this.onCommand(commandParameters));
            } catch (final CommandReturnException e) {
                e.getEmbedBuilder()
                        .ifPresent(embedBuilder -> this.sendTimedMessage(commandParameters, embedBuilder));
                commandResult = e.getCommandResult();

            } catch (final Exception e) {
                log.error("Exception during command execution", e);
                this.sendErrorMessage(commandParameters, "Unknown");

                // Sentry error
                Sentry.captureEvent(new SentryEventBuilder()
                        .addBreadcrumb(new BreadcrumbBuilder()
                                .setCategory("Command")
                                .setData("channelId", String.valueOf(commandParameters.getChannelDb().getDiscordId()))
                                .setData("userId", String.valueOf(commandParameters.getUserDb().getDatabaseId()))
                                .setData("args", Arrays.toString(commandParameters.getArgs()))
                                .setData(COMMAND, this.name)
                                .build())
                        .setLevel(SentryLevel.ERROR)
                        .setMessage("Command Exception")
                        .setLogger(this.getClass().getName())
                        .setThrowable(e)
                        .build());

                commandResult = CommandResult.ERROR;
            }

            // Command post event
            this.getEventModule().executeEvent(new CommandExecutionEvent.Post(
                    this,
                    commandParameters,
                    commandResult == null ? CommandResult.MISSING : commandResult
            ));
        } finally {
            fullTimer.observeDuration();
        }
    }

    public boolean hasPermission(@NonNull final CommandParameters commandParameters) {
        // Permission check
        if (this.getPermissionId() != -1 && !commandParameters.getUserDb()
                .getAllPermissionIds()
                .contains(this.getPermissionId())) {
            return false;
        }

        // Properties Check
        for (final CommandProperty<?> commandProperty : this.getPropertiesMap().values()) {
            if (!commandProperty.onPermissionCheck(this, commandParameters)) {
                return false;
            }
        }
        return true;
    }

    protected void addProperty(final @NonNull CommandProperty<?> property) {
        this.getPropertiesMap().put((Class<? extends CommandProperty<?>>) property.getClass(), property);
    }

    protected void addProperties(final CommandProperty<?>... properties) {
        for (final CommandProperty<?> property : properties) {
            this.addProperty(property);
        }
    }

    public <V> V getPropertyValueOrDefault(@NonNull final Class<? extends CommandProperty<V>> propertyClass,
                                           @Nullable final V defaultValue) {
        final CommandProperty<V> property = (CommandProperty<V>) this.propertiesMap.get(propertyClass);
        if (property != null) {
            return property.getValue();
        }
        return defaultValue;
    }

    public String[] getAliasNames() {
        return this.aliasNames.clone();
    }

    protected void setPermission(@NonNull final String permission) {
        this.permissionId = this.getPermissionsModule().addPermission(permission);
    }


    // Old Stuff
    public List<String> getFormattedExampleCommands() {
        final String mainCommand = this.getCommandModule().getMainCommand();

        final String[] values = this.getPropertyValueOrDefault(ExampleCommandsCommandProperty.class, new String[0]);
        final List<String> exampleCommands = new ArrayList<>(values.length);
        for (final String exampleCommand : values) {
            exampleCommands.add(String.join(" ", mainCommand, this.name, exampleCommand));
        }
        return exampleCommands;
    }

    public void sendMissingArgsMessage(@NonNull final CommandParameters commandParameters) {
        this.sendMissingArgsMessage(
                commandParameters,
                this.getPropertyValueOrDefault(MinArgCommandProperty.class, 0)
        );
    }

    protected void sendMissingArgsMessage(@NonNull final CommandParameters commandParameters,
                                          final int requiredSyntaxLength) {
        final String[] args = commandParameters.getArgs();
        final String[] splitSyntax = this.syntax.split(" ");

        final StringJoiner requiredSyntax = new StringJoiner(" ");
        for (int index = 0; Math.min(requiredSyntaxLength, splitSyntax.length) > index; index++) {
            requiredSyntax.add(args.length > index ? args[index] : MarkdownUtil.bold(splitSyntax[index]));
        }

        final String exampleCommands = String.join("\n", this.getFormattedExampleCommands());
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Missing Args")
                        .setDescription("You are missing a few required arguments.\n"
                                + "It is required that you enter the bold arguments.")
                        .addField("Required Syntax", requiredSyntax.toString(), false)
                        .addField("Command Syntax", this.getSyntax(), false)
                        .addField("Example Commands", exampleCommands, false, !exampleCommands.isEmpty())
        );
    }

    protected void sendErrorMessage(@NonNull final CommandParameters commandParameters,
                                    @NonNull final String error) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Something went wrong")
                        .setDescription("Something went wrong while executing this command.")
                        .addField("Command", this.getName(), false)
                        .addField("Args", String.join(" ", commandParameters.getArgs()), false)
                        .addField(ERROR, error, false)
        );
    }

    protected void sendHelpMessage(@NonNull final CommandParameters commandParameters,
                                   @NonNull final String userArg,
                                   final int argPos,
                                   @NonNull final String argName,
                                   @Nullable final Class<? extends AbstractCommand> commandClass,
                                   @Nullable final String[] newArgs,
                                   @NonNull final List<String> similarNames) {
        final AbstractCommand command;
        if (commandClass == null) {
            command = null;
        } else {
            command = this.getCommandModule().getCommand(commandClass).orElse(null);

        }

        final Map<String, EmoteAction> emotes = new LinkedHashMap<>();
        final StringBuilder helpDescription = new StringBuilder(String.format(
                "%s is not a valid %s.%n",
                MarkdownUtil.monospace(userArg),
                argName
        ));

        if (similarNames.isEmpty() && command != null) {
            helpDescription.append(String.format(
                    "Use the %s command or click the %s emote to see all %ss.",
                    MarkdownUtil.bold(
                            String.join(" ",
                                    this.getCommandModule().getMainCommand(),
                                    command.getName(),
                                    String.join(" ", newArgs)
                            )
                    ),
                    DiscordEmotes.FOLDER.getEmote(),
                    argName
            ));
        } else {
            helpDescription.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarNames.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                helpDescription.append(String.format(
                        "%s %s%n",
                        emote,
                        similarNames.get(index)
                ));

                final String[] newArgsParameter = commandParameters.getArgs();
                newArgsParameter[argPos] = similarNames.get(index);
                final CommandParameters newCommandParameters = CommandParameters.of(commandParameters, newArgsParameter);

                emotes.put(emote, new CommandEmoteAction(this.getClass(), newCommandParameters));
            }

            if (command != null) {
                helpDescription.append(String.format(
                        "%n%s %s",
                        DiscordEmotes.FOLDER.getEmote(),
                        MarkdownUtil.bold("All " + argName + "s")
                ));
            }
        }

        if (command != null) {
            final CommandParameters newCommandParameters = CommandParameters.of(commandParameters, newArgs);
            emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteAction(command.getClass(), newCommandParameters));
        }

        this.sendEmoteMessage(
                commandParameters,
                "Invalid " + StringUtilities.capitalize(argName),
                helpDescription.toString(),
                emotes
        );
    }

    // Checks
    protected void checkArgLength(@NonNull final CommandParameters commandParameters,
                                  final int length) {
        if (length > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(
                    commandParameters,
                    Math.max(this.getPropertyValueOrDefault(MinArgCommandProperty.class, length), length)
            );
            throw new CommandReturnException(CommandResult.MISSING_ARGS);
        }
    }

    // Args
    protected String getArg(final CommandParameters commandParameters, final int argPos) {
        return commandParameters.getArgs()[argPos];
    }

    protected String getArgOrDefault(final CommandParameters commandParameters,
                                     final int argPos,
                                     final String defaultValue) {
        if (argPos >= commandParameters.getArgs().length || commandParameters.getArgs()[argPos] == null) {
            return defaultValue;
        } else {
            return this.getArg(commandParameters, argPos);
        }
    }

    public User getDiscordUserThrow(@NonNull final CommandParameters commandParameters,
                                    final int argPos) {
        final String discordUserName = this.getArg(commandParameters, argPos);
        final Matcher userIdMatcher = DISCORD_USER_ID_PATTERN.matcher(discordUserName);
        if (userIdMatcher.find()) {
            final User user = commandParameters.getUserDb()
                    .getUserDbModule()
                    .getDiscordUserCache()
                    .get(Long.valueOf(userIdMatcher.group(2)));
            if (user != null) {
                return user;
            }
        }

        throw new CommandReturnException(
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid User")
                        .setDescription(MarkdownUtil.monospace(discordUserName) + " is not a valid discord user.")
        );
    }

    public String getFromListIgnoreCaseThrow(@NonNull final CommandParameters commandParameters,
                                             final int argPos,
                                             @NonNull final List<String> possibleArguments) {
        final String userArg = this.getArg(commandParameters, argPos);
        for (final String possibleArg : possibleArguments) {
            if (possibleArg.equalsIgnoreCase(userArg)) {
                return possibleArg;
            }
        }

        this.sendHelpMessage(
                commandParameters,
                userArg,
                argPos,
                "argument",
                null,
                null,
                possibleArguments
        );
        throw new CommandReturnException();
    }

    public <E extends Enum> E getFromEnumIgnoreCaseThrow(@NonNull final CommandParameters commandParameters,
                                                         final int argPos,
                                                         @NonNull final E[] enumValue) {
        final String userArg = this.getArg(commandParameters, argPos);
        final Optional<E> arg = EnumUtilities.getIgnoreCase(userArg, enumValue);
        if (arg.isPresent()) {
            return arg.get();
        }

        this.sendHelpMessage(commandParameters,
                userArg,
                argPos,
                "argument",
                null,
                null,
                EnumUtilities.getPrettyNames(enumValue)
        );
        throw new CommandReturnException();
    }

    public AbstractCommand getCommandThrow(@NonNull final CommandParameters commandParameters,
                                           final int argPos) {
        final String commandName = this.getArg(commandParameters, argPos);
        final Optional<AbstractCommand> command = this.getCommandModule().getCommand(commandName);
        if (command.isPresent()) {
            return command.get();
        }

        final List<AbstractCommand> similarCommands = DataUtilities.getSimilarityList(
                commandName,
                this.getCommandModule().getCommandsWithPerms(commandParameters),
                AbstractCommand::getName,
                0.6,
                3
        );
        if (!similarCommands.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarCommands.get(0);
        }

        AbstractCommand.this.sendHelpMessage(
                commandParameters,
                commandName,
                argPos,
                COMMAND,
                HelpCommand.class,
                new String[0],
                DataUtilities.convertToStringList(similarCommands, AbstractCommand::getName)
        );
        throw new CommandReturnException();
    }

    public Rank getRankThrow(@NonNull final CommandParameters commandParameters,
                             final int position) {
        final String userInput = this.getArg(commandParameters, position);
        final Optional<Rank> rankOpt = this.getRankModule().getRank(userInput);
        if (rankOpt.isPresent()) {
            return rankOpt.get();
        }

        throw new CommandReturnException(
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle(ERROR)
                        .setDescription(MarkdownUtil.monospace(userInput) + " is not a valid rank.")
        );
    }

    public int getPermissionIdThrow(@NonNull final CommandParameters commandParameters,
                                    final int argPos) {
        final String permArg = this.getArg(commandParameters, argPos);

        // Check for command names
        final Optional<AbstractCommand> commandOpt = this.getCommandModule().getCommand(permArg);
        if (commandOpt.isPresent()) {
            final AbstractCommand command = commandOpt.get();
            if (command.getPermissionId() == -1) {
                throw new CommandReturnException(
                        DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR)
                                .setDescription(MarkdownUtil.monospace(command.getName()) + " command has no permission.")
                );
            }

            return command.getPermissionId();
        }

        // check for setting names
        final Optional<SettingModule> settingModuleOpt = this.getModuleManager().getModule(SettingModule.class);
        if (settingModuleOpt.isPresent()) {
            final Optional<AbstractSetting<?>> settingOpt = settingModuleOpt.get().getSetting(permArg);
            if (settingOpt.isPresent()) {
                return settingOpt.get().getPermissionId();
            }
        }

        return this.getPermissionsModule()
                .getPermissionId(permArg)
                .orElseThrow(() -> new CommandReturnException(
                        DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR)
                                .setDescription(MarkdownUtil.monospace(permArg) + " is not a valid permission.")
                ));
    }
}
