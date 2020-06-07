package de.timmi6790.statsbotdiscord.modules.command;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.datatypes.StatEmbedBuilder;
import de.timmi6790.statsbotdiscord.events.CommandExecutionEvent;
import de.timmi6790.statsbotdiscord.events.MessageReceivedIntEvent;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.HelpCommand;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesString;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public abstract class AbstractCommand {
    private final static Pattern DISCORD_USER_ID_PATTERN = Pattern.compile("^(<@[!&])?(\\d*)>?$");
    private final static Pattern DISCORD_USER_TAG_PATTERN = Pattern.compile("^(.{2,32})#(\\d{4})$");

    private final static String GET_COMMAND_ID = "SELECT id FROM `command` WHERE command_name = :command_name LIMIT 1;";
    private final static String INSERT_NEW_COMMAND = "INSERT INTO command(command_name) VALUES(:command_name);";

    private final static String INSERT_COMMAND_LOG = "INSERT INTO command_log(command_id, command_cause_id, command_status_id, in_guild) VALUES(:commandId, " +
            "(SELECT id FROM command_cause WHERE cause_name = :causeName LIMIT 1), (SELECT id FROM command_status WHERE status_name = :statusName LIMIT 1), :inGuild);";

    private final int dbId;
    private final String name;
    private String category;
    private final String description;

    private final List<String> exampleCommands = new ArrayList<>();

    private final String syntax;
    private int minArgs = 0;

    private final String[] aliasNames;

    private boolean defaultPerms = false;
    private String permissionNode;
    private final List<Permission> userDiscordPermissions = new ArrayList<>();

    private final Set<Permission> requiredBotDiscordPermissions = new HashSet<>();
    private boolean allowBots = true;
    private boolean allowPrivateMessages = true;

    public AbstractCommand(final String name, final String category, final String description, final String syntax, final String... aliasNames) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.syntax = syntax;
        this.aliasNames = aliasNames;
        this.dbId = this.getCommandDbId();
    }

    private int getCommandDbId() {
        return StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery(GET_COMMAND_ID)
                        .bind("command_name", this.name)
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(INSERT_NEW_COMMAND)
                                    .bind("command_name", this.name)
                                    .execute();

                            return handle.createQuery(GET_COMMAND_ID)
                                    .bind("command_name", this.name)
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }

    protected abstract CommandResult onCommand(CommandParameters commandParameters);

    protected boolean onPermissionCheck(final CommandParameters commandParameters) {
        return false;
    }

    public void runCommand(final CommandParameters commandParameters, final CommandCause commandCause) {
        // Check command specific permissions discord bot perms
        if (commandParameters.getEvent().isFromGuild()) {
            for (final Permission permission : this.getRequiredBotDiscordPermissions()) {
                if (!commandParameters.getDiscordChannelPermissions().contains(permission)) {
                    this.sendTimedMessage(commandParameters, this.getMissingPermsMessage(permission, commandParameters.getEvent()), 150);
                    return;
                }
            }
        }

        // Command pre event
        final CommandExecutionEvent.Pre commandExecutionPre = new CommandExecutionEvent.Pre(this, commandParameters);
        StatsBot.getEventManager().callEvent(commandExecutionPre);

        CommandResult commandResult = null;
        if (!this.hasPermission(commandParameters)) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Missing perms")
                            .setDescription("You don't have the permissions to run this command."),
                    90
            );
            commandResult = CommandResult.NO_PERMS;

        } else if (this.minArgs > commandParameters.getArgs().length) {
            final String[] args = commandParameters.getArgs();
            final String[] syntax = this.syntax.split(" ");

            final StringJoiner requiredSyntax = new StringJoiner(" ");
            for (int index = 0; Math.min(this.minArgs, syntax.length) > index; index++) {
                requiredSyntax.add(args.length > index ? args[index] : MarkdownUtil.bold(syntax[index]));
            }

            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters).setTitle("Missing Args")
                            .setDescription("You are missing a few required arguments.\nIt is required that you enter the bold arguments.")
                            .addField("Required Syntax", requiredSyntax.toString(), false)
                            .addField("Command Syntax", this.getSyntax(), false),
                    90
            );
            commandResult = CommandResult.MISSING_ARGS;
        }

        // Run the command if all other checks failed
        if (commandResult == null) {
            try {
                commandResult = this.onCommand(commandParameters);
            } catch (final CommandReturnException e) {
                e.getEmbedBuilder().ifPresent(embedBuilder -> this.sendTimedMessage(commandParameters, embedBuilder, 90));
                commandResult = e.getCommandResult();

            } catch (final Exception e) {
                e.printStackTrace();

                // Sentry error
                final Map<String, String> data = commandParameters.getSentryMap();
                data.put("command", this.name);

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

                StatsBot.getSentry().sendEvent(eventBuilder);

                this.sendErrorMessage(commandParameters, "Unknown");
                commandResult = CommandResult.ERROR;
            }
        }
        final CommandResult finalCommandResult = commandResult != null ? commandResult : CommandResult.MISSING;

        // Log commands
        StatsBot.getDatabase().useHandle(handle ->
                handle.createUpdate(INSERT_COMMAND_LOG)
                        .bind("commandId", this.dbId)
                        .bind("causeName", commandCause.name().toLowerCase())
                        .bind("statusName", finalCommandResult.name().toLowerCase())
                        .bind("inGuild", commandParameters.getEvent().isFromGuild())
                        .execute());

        // Command post event
        final CommandExecutionEvent.Post commandExecutionPost = new CommandExecutionEvent.Post(this, commandParameters, finalCommandResult);
        StatsBot.getEventManager().callEvent(commandExecutionPost);
    }

    public final boolean hasPermission(final CommandParameters commandParameters) {
        // Check for other bots
        if (!this.allowBots && commandParameters.getEvent().getAuthor().isBot()) {
            return false;
        }

        if (this.defaultPerms || commandParameters.getUserDb().getPermissionNodes().contains(this.permissionNode)) {
            if (this.userDiscordPermissions.isEmpty() || !commandParameters.getEvent().isFromGuild()) {
                return true;
            }

            for (final Permission permission : commandParameters.getEvent().getGuild().getMember(commandParameters.getEvent().getAuthor()).getPermissions()) {
                if (this.userDiscordPermissions.contains(permission)) {
                    return true;
                }
            }

            return false;
        }

        return this.onPermissionCheck(commandParameters);
    }

    protected void setPermission(final String permission) {
        this.permissionNode = permission;
    }

    protected void addDiscordPermission(final Permission permission) {
        this.requiredBotDiscordPermissions.add(permission);
    }

    protected void addDiscordPermissions(final Permission... permissions) {
        this.requiredBotDiscordPermissions.addAll(Arrays.asList(permissions));
    }

    protected void addExampleCommands(final String... exampleCommands) {
        this.exampleCommands.addAll(Arrays.asList(exampleCommands));
    }

    public StatEmbedBuilder getEmbedBuilder(final CommandParameters commandParameters) {
        return UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters);
    }

    protected void sendErrorMessage(final CommandParameters commandParameters, final String error) {
        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters).setTitle("Something went wrong")
                        .setDescription("Something went wrong while executing this command.")
                        .addField("Command", this.getName(), false)
                        .addField("Args", String.join(" ", commandParameters.getArgs()), false)
                        .addField("Error", error, false),
                90);
    }

    protected void sendEmoteMessage(final CommandParameters commandParameters, final String title, final String description, final Map<String, AbstractEmoteReaction> emotes) {
        this.sendEmoteMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(title)
                        .setDescription(description),
                emotes);
    }

    protected void sendEmoteMessage(final CommandParameters commandParameters, final EmbedBuilder embedBuilder, final Map<String, AbstractEmoteReaction> emotes) {
        commandParameters.getEvent().getChannel().sendMessage(
                embedBuilder.setFooter("↓ Click Me!").build())
                .queue(message -> {
                            if (!emotes.isEmpty()) {
                                final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, commandParameters.getEvent().getAuthor().getIdLong(),
                                        commandParameters.getEvent().getChannel().getIdLong());
                                StatsBot.getEmoteReactionManager().addEmoteReactionMessage(message, emoteReactionMessage);
                            }

                            message.delete().queueAfter(90, TimeUnit.SECONDS);
                        }
                );
    }

    protected void sendTimedMessage(final CommandParameters commandParameters, final EmbedBuilder embedBuilder, final int deleteTime) {
        commandParameters.getDiscordChannel()
                .sendMessage(embedBuilder.build())
                .delay(deleteTime, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();
    }

    protected void sendHelpMessage(final CommandParameters commandParameters, final String userArg, final int argPos, final String argName,
                                   final AbstractCommand command, final String[] newArgs, final List<String> similarNames) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder();
        description.append(MarkdownUtil.monospace(userArg)).append(" is not a valid ").append(argName).append(".\n");

        if (similarNames.isEmpty()) {
            description.append("Use the ").append(MarkdownUtil.bold(StatsBot.getCommandManager().getMainCommand() + " " + command.getName() + " " + String.join(" ", newArgs)))
                    .append(" command or click the ").append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all ").append(argName).append("s.");

        } else {
            description.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarNames.size() > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(emote).append(" ").append(MarkdownUtil.bold(similarNames.get(index))).append("\n");

                final CommandParameters newCommandParameters = new CommandParameters(commandParameters);
                newCommandParameters.getArgs()[argPos] = similarNames.get(index);

                emotes.put(emote, new CommandEmoteReaction(this, newCommandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(MarkdownUtil.bold("All " + argName + "s"));
        }

        final CommandParameters newCommandParameters = new CommandParameters(commandParameters);
        newCommandParameters.setArgs(newArgs);
        emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(command, newCommandParameters));

        this.sendEmoteMessage(commandParameters, "Invalid " + UtilitiesString.capitalize(argName), description.toString(), emotes);
    }

    private EmbedBuilder getMissingPermsMessage(final Permission permission, final MessageReceivedIntEvent event) {
        return UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                .setTitle("Missing Permission")
                .setDescription("The bot is missing the " + MarkdownUtil.monospace(permission.getName()) + " permission.");
    }

    protected User getDiscordUser(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Matcher userIdMatcher = DISCORD_USER_ID_PATTERN.matcher(name);
        if (userIdMatcher.find()) {
            final User user = StatsBot.getDiscord().getUserById(userIdMatcher.group(2));
            if (user != null) {
                return user;
            }
        }

        if (DISCORD_USER_TAG_PATTERN.matcher(name).find()) {
            final User user = StatsBot.getDiscord().getUserByTag(name);
            if (user != null) {
                return user;
            }
        }

        throw new CommandReturnException(
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid User")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a valid discord user.")
        );
    }

    protected AbstractCommand getCommand(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<AbstractCommand> command = StatsBot.getCommandManager().getCommand(name);
        if (command.isPresent()) {
            return command.get();
        }

        final AbstractCommand[] similarCommands = StatsBot.getCommandManager().getSimilarCommands(commandParameters, name, 0.6, 3).toArray(new AbstractCommand[0]);
        if (similarCommands.length != 0 && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarCommands[0];
        }

        final List<String> similarNames = new ArrayList<>();
        for (final AbstractCommand similarCommand : similarCommands) {
            similarNames.add(similarCommand.getName());
        }

        final AbstractCommand helpCommand = StatsBot.getCommandManager().getCommand(HelpCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "command", helpCommand, new String[0], similarNames);
        throw new CommandReturnException();
    }
}
