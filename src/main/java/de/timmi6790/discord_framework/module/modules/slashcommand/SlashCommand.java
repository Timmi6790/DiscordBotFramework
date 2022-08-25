package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.utilities.sentry.BreadcrumbBuilder;
import de.timmi6790.discord_framework.utilities.sentry.SentryEventBuilder;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Log4j2
public abstract class SlashCommand {
    private final String name;
    private String[] aliases = new String[0];
    private final String description;

    private boolean requiresPermission = false;

    private final List<Option<?>> options = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private final Map<Class<? extends CommandProperty<?>>, CommandProperty<?>> properties = new HashMap<>();

    public SlashCommand(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    protected void setAliases(final String... aliases) {
        this.aliases = aliases;
    }

    protected void addOptions(final Option<?>... options) {
        this.options.addAll(List.of(options));
    }

    protected abstract CommandResult onCommand(SlashCommandParameters parameters);

    public void executeCommand(final SlashCommandParameters commandParameters) {
        // User ban check
        /*
        if (commandParameters.getUserDb().isBanned()) {
            MessageUtilities.sendUserBanMessage(commandParameters);
            return;
        }

        // Guild ban check
        if (commandParameters.getGuildDb().isBanned()) {
            MessageUtilities.sendGuildBanMessage(commandParameters);
            return;
        }

        // Command perms check
        if (!this.canExecute(commandParameters)) {
            MessageUtilities.sendMissingPermissionsMessage(commandParameters);
            return;
        }

         */

        /*
        // Property checks
        for (final CommandProperty<?> commandProperty : this.properties.values()) {
            if (!commandProperty.onCommandExecution(this, commandParameters)) {
                return;
            }
        }
         */

        // Command pre event
        // this.getEventModule().executeEvent(new PreCommandExecutionEvent(this, commandParameters));

        commandParameters.getEvent().deferReply().queue();

        CommandResult commandResult;
        final long startTime = System.nanoTime();
        final long executionTime;
        try {
            commandResult = this.onCommand(commandParameters);
        } catch (final CommandReturnException exception) {
            commandResult = exception.getCommandResult();
        } catch (final Exception exception) {
            log.error("Exception during command execution", exception);
            Sentry.captureEvent(new SentryEventBuilder()
                    .addBreadcrumb(new BreadcrumbBuilder()
                            .setCategory("Command")
                            .setData("channelId", String.valueOf(commandParameters.getChannelDb().getDiscordId()))
                            .setData("userId", String.valueOf(commandParameters.getUserDb().getDiscordId()))
                            .setData("args", commandParameters.getEvent().getOptions().stream().map(OptionMapping::getAsString).collect(Collectors.joining(", ")))
                            .setData("command", this.name)
                            .build())
                    .setLevel(SentryLevel.ERROR)
                    .setMessage("Command Exception")
                    .setLogger(this.getClass().getName())
                    .setThrowable(exception)
                    .build());
            commandResult = BaseCommandResult.EXCEPTION;
        } finally {
            executionTime = System.nanoTime() - startTime;
        }

        // Assure that the command result is never null
        if (commandResult == null) {
            log.warn("Returned invalid CommandResult of null");
            commandResult = BaseCommandResult.UNKNOWN;
        }

        /*
        // Command post event
        this.getEventModule().executeEvent(
                new PostCommandExecutionEvent(
                        this,
                        commandParameters,
                        commandResult,
                        executionTime
                )
        );
         */
    }
}
