package de.timmi6790.discord_framework.module.modules.command;

import de.timmi6790.discord_framework.module.modules.command.events.PostCommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.command.events.PreCommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.ExampleCommandsProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.RequiredDiscordBotPermsProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.MessageUtilities;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.utilities.sentry.BreadcrumbBuilder;
import de.timmi6790.discord_framework.utilities.sentry.SentryEventBuilder;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.Permission;

import java.util.*;
import java.util.function.Supplier;

@Data
@Log4j2
public abstract class Command {
    private static final EnumSet<Permission> MINIMUM_DISCORD_PERMISSIONS = EnumSet.of(
            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_EMBED_LINKS
    );

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(AccessLevel.PROTECTED)
    private final CommandModule commandModule;

    private final String name;
    @Getter(AccessLevel.NONE)
    private final Map<Class<? extends CommandProperty<?>>, CommandProperty<?>> properties = new HashMap<>();
    private int permissionId = -1;

    protected Command(final String name, final CommandModule commandModule) {
        this.name = name;
        this.commandModule = commandModule;
    }

    protected abstract CommandResult onCommand(CommandParameters commandParameters);

    protected boolean addProperty(final CommandProperty<?> property) {
        final Class<? extends CommandProperty<?>> propertyClass = (Class<? extends CommandProperty<?>>) property.getClass();
        if (this.properties.containsKey(propertyClass)) {
            log.warn(
                    "[{}] The property {} is already registered",
                    this.name,
                    propertyClass
            );
            return false;
        }

        this.properties.put(propertyClass, property);
        return true;
    }

    protected void addProperties(final CommandProperty<?>... properties) {
        for (final CommandProperty<?> property : properties) {
            this.addProperty(property);
        }
    }

    protected EventModule getEventModule() {
        return this.commandModule.getEventModule();
    }

    protected PermissionsModule getPermissionsModule() {
        return this.commandModule.getPermissionsModule();
    }

    protected Optional<MetricModule> getMetricModule() {
        return this.commandModule.getMetricModule();
    }

    public boolean hasDefaultPermission() {
        return this.permissionId == -1;
    }

    public boolean canExecute(final CommandParameters commandParameters) {
        // Permission check
        if (!commandParameters.getUserDb()
                .getAllPermissionIds()
                .contains(this.getPermissionId())) {
            return false;
        }

        // Properties Check
        for (final CommandProperty<?> commandProperty : this.properties.values()) {
            if (!commandProperty.onPermissionCheck(this, commandParameters)) {
                return false;
            }
        }
        return true;
    }

    public void executeCommand(final CommandParameters commandParameters) {
        // User ban check
        if (commandParameters.getUserDb().isBanned()) {
            MessageUtilities.sendUserBanMessage(commandParameters);
            return;
        }

        // Guild ban check
        if (commandParameters.getGuildDb().isBanned()) {
            MessageUtilities.sendGuildBanMessage(commandParameters);
            return;
        }

        // Discord perms check
        if (commandParameters.isGuildCommand()) {
            final EnumSet<Permission> requiredDiscordPerms = this.getPropertyValueOrDefault(
                    RequiredDiscordBotPermsProperty.class,
                    () -> EnumSet.noneOf(Permission.class)
            );
            final EnumSet<Permission> missingDiscordPerms = EnumSet.copyOf(MINIMUM_DISCORD_PERMISSIONS);
            missingDiscordPerms.addAll(requiredDiscordPerms);

            final Set<Permission> permissions = commandParameters.getDiscordPermissions();
            missingDiscordPerms.removeIf(permissions::contains);

            // Send error message
            if (!missingDiscordPerms.isEmpty()) {
                MessageUtilities.sendMissingDiscordPermissionMessage(commandParameters, missingDiscordPerms);
                return;
            }
        }

        // Command perms check
        if (!this.canExecute(commandParameters)) {
            MessageUtilities.sendMissingPermissionsMessage(commandParameters);
            return;
        }

        // Property checks
        for (final CommandProperty<?> commandProperty : this.properties.values()) {
            if (!commandProperty.onCommandExecution(this, commandParameters)) {
                return;
            }
        }

        // Command pre event
        this.getEventModule().executeEvent(new PreCommandExecutionEvent(this, commandParameters));

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
                            .setData("args", Arrays.toString(commandParameters.getArgs()))
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
        this.getEventModule().executeEvent(
                new PostCommandExecutionEvent(
                        this,
                        commandParameters,
                        commandResult,
                        executionTime
                )
        );
    }

    public <V extends CommandProperty<?>> Optional<V> getProperty(final Class<V> propertyClass) {
        final V property = (V) this.properties.get(propertyClass);
        return Optional.ofNullable(property);
    }

    public Set<CommandProperty<?>> getProperties() {
        return new HashSet<>(this.properties.values());
    }

    public <V> V getPropertyValueOrDefault(final Class<? extends CommandProperty<V>> propertyClass,
                                           final Supplier<V> defaultSupplier) {
        return this.getProperty(propertyClass)
                .map(CommandProperty::getValue)
                .orElseGet(defaultSupplier);
    }

    protected void checkArgLength(final CommandParameters commandParameters,
                                  final int length) {
        if (length > commandParameters.getArgs().length) {
            MessageUtilities.sendMissingArgsMessage(
                    commandParameters,
                    this.getPropertyValueOrDefault(SyntaxProperty.class, () -> ""),
                    length,
                    this.getPropertyValueOrDefault(ExampleCommandsProperty.class, () -> new String[0])
            );
            throw new CommandReturnException();
        }
    }
}
