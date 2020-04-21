package de.timmi6790.statsbotdiscord.modules.command;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.events.EventCommandExecution;
import de.timmi6790.statsbotdiscord.events.EventMessageReceived;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
@EqualsAndHashCode
@ToString
@Setter
public abstract class AbstractCommand {
    private final int dbId;
    private final String name;
    private final String category;
    private final String description;

    private final String syntax;
    private int minArgs = 0;

    private final String[] aliasNames;

    private boolean defaultPerms = false;
    private final Set<String> permissionNodes = new HashSet<>();
    private final Set<Permission> discordPermissions = new HashSet<>();

    public AbstractCommand(final String name, final String category, final String description, final String syntax, final String... aliasNames) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.syntax = syntax;
        this.aliasNames = aliasNames;

        this.dbId = StatsBot.getDatabase().withHandle(handle -> {
            final Optional<Integer> dbId = handle.createQuery("SELECT id FROM command " +
                    "WHERE command.command_name = :commandName " +
                    "LIMIT 1;")
                    .bind("commandName", this.name)
                    .mapTo(Integer.class)
                    .findFirst();

            if (dbId.isPresent()) {
                return dbId.get();
            }

            handle.createUpdate("INSERT INTO command(command_name) VALUES(:commandName)")
                    .bind("commandName", this.name)
                    .execute();

            // Should always return the id
            return handle.createQuery("SELECT id FROM command " +
                    "WHERE command.command_name = :commandName " +
                    "LIMIT 1;")
                    .bind("commandName", this.name)
                    .mapTo(Integer.class)
                    .first();
        });
    }

    protected abstract CommandResult onCommand(CommandParameters commandParameters);

    protected boolean onPermissionCheck(final CommandParameters commandParameters) {
        return false;
    }

    public void runCommand(final CommandParameters commandParameters) {
        // Check command specific permissions
        for (final Permission permission : this.getDiscordPermissions()) {
            if (!commandParameters.getDiscordChannelPermissions().contains(permission)) {
                commandParameters.getDiscordChannel().sendMessage(this.getMissingPermsMessage(permission, commandParameters.getEvent()))
                        .delay(150, TimeUnit.SECONDS)
                        .flatMap(Message::delete)
                        .queue();
                return;
            }
        }

        // Command pre event
        final EventCommandExecution.Pre commandExecutionPre = new EventCommandExecution.Pre(this, commandParameters);
        StatsBot.getEventManager().callEvent(commandExecutionPre);

        CommandResult commandResult = null;
        if (!this.hasPermission(commandParameters)) {
            commandParameters.getDiscordChannel().sendMessage(
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle("Missing perms")
                            .setDescription("You don't have the")
                            .build())
                    .delay(90, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            commandResult = CommandResult.NO_PERMS;

        } else if (this.minArgs > commandParameters.getArgs().length) {
            commandParameters.getDiscordChannel()
                    .sendMessage(
                            UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters).setTitle("Missing args")
                                    .setDescription("Something went horrible wrong while executing this command.")
                                    .addField("Command", this.getName(), false)
                                    .addField("Args", String.join(" ", commandParameters.getArgs()), false)
                                    .build())
                    .delay(90, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            commandResult = CommandResult.ERROR;
        }

        // Run the command if all other checks failed
        if (commandResult == null) {
            try {
                commandResult = this.onCommand(commandParameters);
            } catch (final CommandReturnException e) {
                e.getEmbedBuilder().ifPresent(embedBuilder -> commandParameters.getDiscordChannel()
                        .sendMessage(embedBuilder.build())
                        .delay(90, TimeUnit.SECONDS)
                        .flatMap(Message::delete)
                        .queue());
                commandResult = e.getCommandResult();

            } catch (final Exception e) {
                // Sentry error
                final Map<String, String> data = commandParameters.getSentryMap();
                data.put("commandId", String.valueOf(this.dbId));

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

                e.printStackTrace();

                this.sendErrorMessage(commandParameters, "Unknown");
                commandResult = CommandResult.ERROR;
            }
        }

        commandResult = commandResult != null ? commandResult : CommandResult.MISSING;

        // Log in db
        // TODO: Add logging

        // Command post event
        final EventCommandExecution.Post commandExecutionPost = new EventCommandExecution.Post(this, commandParameters, commandResult);
        StatsBot.getEventManager().

                callEvent(commandExecutionPost);

    }

    public final boolean hasPermission(final CommandParameters commandParameters) {
        if (this.defaultPerms) {
            return true;
        }

        for (final String permission : commandParameters.getUser().getPermissionNodes()) {
            if (this.permissionNodes.contains(permission)) {
                return true;
            }
        }

        return this.onPermissionCheck(commandParameters);
    }

    protected void addPermission(final String permission) {
        this.permissionNodes.add(permission);
    }

    protected void addDiscordPermission(final Permission permission) {
        this.discordPermissions.add(permission);
    }

    protected void addDiscordPermissions(final Permission... permissions) {
        this.discordPermissions.addAll(Arrays.asList(permissions));
    }

    protected void sendErrorMessage(final CommandParameters commandParameters, final String error) {
        commandParameters.getDiscordChannel()
                .sendMessage(
                        UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters).setTitle("Something went wrong")
                                .setDescription("Something went wrong while executing this command.")
                                .addField("Command", this.getName(), false)
                                .addField("Args", String.join(" ", commandParameters.getArgs()), false)
                                .addField("Error", error, false)
                                .build())
                .delay(90, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();
    }

    private MessageEmbed getMissingPermsMessage(final Permission permission, final EventMessageReceived event) {
        return UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                .setTitle("Missing Permission")
                .setDescription("The bot is missing the " + MarkdownUtil.monospace(permission.getName()) + " permission.")
                .build();
    }
}
