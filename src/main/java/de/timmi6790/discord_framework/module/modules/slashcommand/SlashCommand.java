package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.modules.slashcommand.events.PostCommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.slashcommand.events.PreCommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.utilities.SlashMessageUtilities;
import de.timmi6790.discord_framework.utilities.sentry.BreadcrumbBuilder;
import de.timmi6790.discord_framework.utilities.sentry.SentryEventBuilder;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Setter
@Log4j2
@RequiredArgsConstructor
public abstract class SlashCommand {
    private final SlashCommandModule module;
    private final String name;
    private final String description;
    
    private final List<Option<?>> options = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private final Map<Class<? extends SlashCommandProperty<?>>, SlashCommandProperty<?>> properties = new HashMap<>();
    private int permissionId = -1;

    protected void addProperty(final SlashCommandProperty<?> property) {
        final Class<? extends SlashCommandProperty<?>> propertyClass = (Class<? extends SlashCommandProperty<?>>) property.getClass();
        this.properties.put(propertyClass, property);
    }

    protected void addProperties(final SlashCommandProperty<?>... properties) {
        for (final SlashCommandProperty<?> property : properties) {
            this.addProperty(property);
        }
    }

    public <V extends SlashCommandProperty<?>> Optional<V> getProperty(final Class<V> propertyClass) {
        final V property = (V) this.properties.get(propertyClass);
        return Optional.ofNullable(property);
    }

    public Set<SlashCommandProperty<?>> getProperties() {
        return new HashSet<>(this.properties.values());
    }

    public <V> V getPropertyValueOrDefault(final Class<? extends SlashCommandProperty<V>> propertyClass,
                                           final Supplier<V> defaultSupplier) {
        return this.getProperty(propertyClass)
                .map(SlashCommandProperty::getValue)
                .orElseGet(defaultSupplier);
    }

    protected void addOptions(final Option<?>... options) {
        this.options.addAll(List.of(options));
    }

    public boolean hasDefaultPermission() {
        return this.permissionId == -1;
    }

    public boolean canExecute(final SlashCommandParameters commandParameters) {
        // Permission check
        if (!commandParameters.getUserDb()
                .getAllPermissionIds()
                .contains(this.getPermissionId())) {
            return false;
        }

        // Properties Check
        for (final SlashCommandProperty<?> commandProperty : this.properties.values()) {
            if (!commandProperty.onPermissionCheck(this, commandParameters)) {
                return false;
            }
        }
        return true;
    }

    protected abstract CommandResult onCommand(SlashCommandParameters parameters);

    public void executeCommand(final SlashCommandParameters commandParameters) {
        // User ban check
        if (commandParameters.getUserDb().isBanned()) {
            SlashMessageUtilities.sendUserBanMessage(commandParameters);
            return;
        }

        // Guild ban check
        if (commandParameters.getGuildDb().isBanned()) {
            SlashMessageUtilities.sendGuildBanMessage(commandParameters);
            return;
        }

        // Command perms check
        if (!this.canExecute(commandParameters)) {
            SlashMessageUtilities.sendMissingPermissionsMessage(commandParameters);
            return;
        }

        // Property checks
        for (final SlashCommandProperty<?> commandProperty : this.properties.values()) {
            if (!commandProperty.onCommandExecution(this, commandParameters)) {
                return;
            }
        }

        // Command pre event
        this.module.getEventModule().executeEvent(new PreCommandExecutionEvent(this, commandParameters));

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
                            .setData("args", commandParameters.getOptions().values().stream().map(DiscordOption::getAsString).collect(Collectors.joining(", ")))
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

        // Command post event
        this.module.getEventModule().executeEvent(
                new PostCommandExecutionEvent(
                        this,
                        commandParameters,
                        commandResult,
                        executionTime
                )
        );
    }

    public <T> void sendArgumentCorrectionMessage(final SlashCommandParameters commandParameters,
                                                  final Option<?> option,
                                                  @Nullable final MainReplaceData mainReplaceData,
                                                  final List<T> similarValues,
                                                  final Function<T, String> valueToString) {
        this.module.sendArgumentCorrectionMessage(
                commandParameters,
                option,
                mainReplaceData,
                this.getClass(),
                similarValues,
                valueToString
        );
    }
}
