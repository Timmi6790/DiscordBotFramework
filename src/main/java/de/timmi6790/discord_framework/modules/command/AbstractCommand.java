package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.datatypes.ListBuilder;
import de.timmi6790.discord_framework.datatypes.MapBuilder;
import de.timmi6790.discord_framework.datatypes.StatEmbedBuilder;
import de.timmi6790.discord_framework.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.GetModule;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.event.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.utilities.EnumUtilities;
import de.timmi6790.discord_framework.utilities.UtilitiesString;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractCommand<T extends AbstractModule> extends GetModule<T> {
    private static final int COMMAND_USER_RATE_LIMIT = 10;
    private static final String INSERT_COMMAND_LOG = "INSERT INTO command_log(command_id, command_cause_id, command_status_id, in_guild) VALUES(:commandId, " +
            "(SELECT id FROM command_cause WHERE cause_name = :causeName LIMIT 1), (SELECT id FROM command_status WHERE status_name = :statusName LIMIT 1), :inGuild);";

    private static final List<Permission> MINIMUM_DISCORD_PERMISSIONS = Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS);

    private static final Pattern DISCORD_USER_ID_PATTERN = Pattern.compile("^(<@[!&])?(\\d*)>?$");
    private static final Pattern DISCORD_USER_TAG_PATTERN = Pattern.compile("^(.{2,32})#(\\d{4})$");

    private Map<Class<? extends CommandProperty<?>>, CommandProperty<?>> propertiesMap = new HashMap<>();

    private int dbId;
    private Class<? extends AbstractModule> commandModule;

    private final String name;
    private int permissionId = -1;

    private String category;
    private String description;
    private final String syntax;
    private final String[] aliasNames;

    public AbstractCommand(final String name, final String category, final String description, final String syntax, final String... aliasNames) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.syntax = syntax;
        this.aliasNames = aliasNames;
    }

    private void logCommand(final CommandParameters commandParameters, final CommandResult commandResult) {
        DiscordBot.getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
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
            e.getEmbedBuilder().ifPresent(embedBuilder -> this.sendTimedMessage(commandParameters, embedBuilder, 90));
            return e.getCommandResult();

        } catch (final Exception e) {
            DiscordBot.getLogger().error(e);
            this.sendErrorMessage(commandParameters, "Unknown");

            // Sentry error
            final Map<String, String> data = new MapBuilder<String, String>(HashMap::new)
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

            if (DiscordBot.getSentry() != null) {
                DiscordBot.getSentry().sendEvent(eventBuilder);
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
        this.permissionId = DiscordBot.getModuleManager()
                .getModuleOrThrow(PermissionsModule.class)
                .addPermission(permission);
    }


    public void runCommand(final CommandParameters commandParameters) {
        // Server ban check
        if (commandParameters.getChannelDb().getGuildDb().isBanned()) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Banned Server")
                            .setDescription("This server is banned from using this service."),
                    90
            );
            return;
        }

        // User ban check
        if (commandParameters.getUserDb().isBanned()) {
            DiscordMessagesUtilities.sendPrivateMessage(
                    commandParameters.getUser(),
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("You are banned")
                            .setDescription("You are banned from using this service.")
            );
            return;
        }

        // I want to have write perms in all channels the commands should work in
        if (commandParameters.isGuildCommand()) {
            final Set<Permission> permissions = commandParameters.getDiscordPermissions();
            final List<Permission> missingPerms =
                    new ListBuilder<Permission>(() -> new ArrayList<>(2))
                            .addAll(MINIMUM_DISCORD_PERMISSIONS)
                            .addAll(this.getPropertyValueOrDefault(RequiredDiscordBotPermsCommandProperty.class, Collections.emptyList()))
                            .build()
                            .stream()
                            .filter(perm -> !permissions.contains(perm))
                            .collect(Collectors.toList());
            if (!missingPerms.isEmpty()) {
                final String perms = missingPerms.stream()
                        .map(perm -> MarkdownUtil.monospace(perm.getName()))
                        .collect(Collectors.joining(","));

                DiscordMessagesUtilities.sendPrivateMessage(
                        commandParameters.getUser(),
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Missing Permission")
                                .setDescription("The bot is missing " + perms + " permission(s).")
                );
                return;
            }
        }

        if (!this.hasPermission(commandParameters)) {
            this.sendMissingPermissionMessage(commandParameters);
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
        final CommandExecutionEvent.Pre commandExecutionPre = new CommandExecutionEvent.Pre(this, commandParameters);
        DiscordBot.getModuleManager()
                .getModule(EventModule.class)
                .ifPresent(eventModule -> eventModule.executeEvent(commandExecutionPre));

        // Run command
        final CommandResult commandResult = this.executeSave(commandParameters);
        this.logCommand(commandParameters, commandResult);

        // Command post event
        final CommandExecutionEvent.Post commandExecutionPost = new CommandExecutionEvent.Post(this, commandParameters, commandResult);
        DiscordBot.getModuleManager()
                .getModule(EventModule.class)
                .ifPresent(eventModule -> eventModule.executeEvent(commandExecutionPost));
    }

    // Old Stuff
    public List<String> getFormattedExampleCommands() {
        final String mainCommand = DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getMainCommand();
        return Arrays.stream(this.getPropertyValueOrDefault(ExampleCommandsCommandProperty.class, new String[0]))
                .map(exampleCommand -> mainCommand + " " + this.name + " " + exampleCommand)
                .collect(Collectors.toList());
    }

    public StatEmbedBuilder getEmbedBuilder(final CommandParameters commandParameters) {
        return DiscordMessagesUtilities.getEmbedBuilder(commandParameters);
    }

    protected void sendMissingPermissionMessage(final CommandParameters commandParameters) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Missing perms")
                        .setDescription("You don't have the permissions to run this command."),
                90
        );
    }

    public void sendMissingArgsMessage(final CommandParameters commandParameters) {
        this.sendMissingArgsMessage(commandParameters, this.getPropertyValueOrDefault(MinArgCommandProperty.class, 0));
    }

    protected void sendMissingArgsMessage(final CommandParameters commandParameters, final int requiredSyntaxLenght) {
        final String[] args = commandParameters.getArgs();
        final String[] syntax = this.syntax.split(" ");

        final StringJoiner requiredSyntax = new StringJoiner(" ");
        for (int index = 0; Math.min(requiredSyntaxLenght, syntax.length) > index; index++) {
            requiredSyntax.add(args.length > index ? args[index] : MarkdownUtil.bold(syntax[index]));
        }

        final String exampleCommands = String.join("\n", this.getFormattedExampleCommands());
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters).setTitle("Missing Args")
                        .setStatDescription("You are missing a few required arguments.\nIt is required that you enter the bold arguments.")
                        .addField("Required Syntax", requiredSyntax.toString(), false)
                        .addField("Command Syntax", this.getSyntax(), false)
                        .addField("Example Commands", exampleCommands, false, !exampleCommands.isEmpty()),
                90
        );
    }

    protected void sendErrorMessage(final CommandParameters commandParameters, final String error) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters).setTitle("Something went wrong")
                        .setDescription("Something went wrong while executing this command.")
                        .addField("Command", this.getName(), false)
                        .addField("Args", String.join(" ", commandParameters.getArgs()), false)
                        .addField("Error", error, false),
                90
        );
    }

    protected void sendEmoteMessage(final CommandParameters commandParameters, final String title, final String description, final Map<String, AbstractEmoteReaction> emotes) {
        this.sendEmoteMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(title)
                        .setDescription(description),
                emotes
        );
    }

    protected void sendEmoteMessage(final CommandParameters commandParameters, final EmbedBuilder embedBuilder, final Map<String, AbstractEmoteReaction> emotes) {
        commandParameters.getTextChannel().sendMessage(
                embedBuilder.setFooter("↓ Click Me!").build())
                .queue(message -> {
                            if (!emotes.isEmpty()) {
                                final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, commandParameters.getUser().getIdLong(),
                                        commandParameters.getTextChannel().getIdLong());
                                DiscordBot.getModuleManager().getModuleOrThrow(EmoteReactionModule.class).addEmoteReactionMessage(message, emoteReactionMessage);
                            }

                            message.delete().queueAfter(90, TimeUnit.SECONDS);
                        }
                );
    }

    protected void sendTimedMessage(final CommandParameters commandParameters, final EmbedBuilder embedBuilder, final int deleteTime) {
        commandParameters.getTextChannel()
                .sendMessage(embedBuilder.build())
                .delay(deleteTime, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
    }

    protected void sendHelpMessage(final CommandParameters commandParameters, final String userArg, final int argPos, final String argName,
                                   final AbstractCommand<?> command, final String[] newArgs, final List<String> similarNames) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder();
        description.append(MarkdownUtil.monospace(userArg)).append(" is not a valid ").append(argName).append(".\n");

        if (similarNames.isEmpty() && command != null) {
            description.append("Use the ").append(MarkdownUtil.bold(DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getMainCommand() + " " + command.getName() + " " + String.join(" ", newArgs)))
                    .append(" command or click the ").append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all ").append(argName).append("s.");

        } else {
            description.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarNames.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(emote).append(" ").append(MarkdownUtil.bold(similarNames.get(index))).append("\n");

                final String[] newArgsParameter = commandParameters.getArgs();
                newArgsParameter[argPos] = similarNames.get(index);
                final CommandParameters newCommandParameters = new CommandParameters(commandParameters, newArgsParameter);
                emotes.put(emote, new CommandEmoteReaction(this, newCommandParameters));
            }

            if (command != null) {
                description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(MarkdownUtil.bold("All " + argName + "s"));
            }
        }

        final CommandParameters newCommandParameters = new CommandParameters(commandParameters, newArgs);
        if (command != null) {
            emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(command, newCommandParameters));
        }

        this.sendEmoteMessage(commandParameters, "Invalid " + UtilitiesString.capitalize(argName), description.toString(), emotes);
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

        AbstractCommand.this.sendHelpMessage(commandParameters, userArg, argPos, "argument", null, null, EnumUtilities.getPrettyNames(enumValue));
        throw new CommandReturnException();
    }

    protected AbstractCommand<?> getCommandThrow(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<AbstractCommand<?>> command = DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getCommand(name);
        if (command.isPresent()) {
            return command.get();
        }

        final List<AbstractCommand<?>> similarCommands = DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getSimilarCommands(commandParameters, name, 0.6, 3);
        if (!similarCommands.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarCommands.get(0);
        }

        AbstractCommand.this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "command",
                DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getCommand(HelpCommand.class).orElse(null),
                new String[0],
                similarCommands.stream().map(AbstractCommand::getName).collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected Rank getRankThrow(final CommandParameters commandParameters, final int position) {
        final String userInput = commandParameters.getArgs()[position];

        return DiscordBot.getModuleManager().getModuleOrThrow(RankModule.class).getRanks()
                .stream()
                .filter(rank -> rank.getName().equalsIgnoreCase(userInput))
                .findAny()
                .orElseThrow(() -> new CommandReturnException(
                        AbstractCommand.this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(userInput) + " is not a valid rank.")
                ));
    }

    public int getPermissionIdThrow(final CommandParameters commandParameters, final int argPos) {
        final String permArg = commandParameters.getArgs()[argPos];
        final Optional<AbstractCommand<?>> commandOpt = DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getCommand(permArg);

        if (commandOpt.isPresent()) {
            final AbstractCommand<?> command = commandOpt.get();
            if (command.getPermissionId() == -1) {
                throw new CommandReturnException(
                        AbstractCommand.this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(command.getName()) + " command has no permission.")
                );
            }

            return command.getPermissionId();
        } else {
            return DiscordBot.getModuleManager().getModuleOrThrow(PermissionsModule.class).getPermissionId(permArg)
                    .orElseThrow(() -> new CommandReturnException(
                            AbstractCommand.this.getEmbedBuilder(commandParameters)
                                    .setTitle("Error")
                                    .setDescription(MarkdownUtil.monospace(permArg) + " is not a valid permission.")
                    ));
        }
    }

    protected User getDiscordUserThrow(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Matcher userIdMatcher = DISCORD_USER_ID_PATTERN.matcher(name);
        if (userIdMatcher.find()) {
            // TODO: Change to queue instead of complete
            final User user = DiscordBot.getDiscord().retrieveUserById(userIdMatcher.group(2)).complete();
            if (user != null) {
                return user;
            }
        }

        if (DISCORD_USER_TAG_PATTERN.matcher(name).find()) {
            final User user = DiscordBot.getDiscord().getUserByTag(name);
            if (user != null) {
                return user;
            }
        }

        throw new CommandReturnException(
                AbstractCommand.this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid User")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a valid discord user.")
        );
    }
}
