package de.timmi6790.discord_framework.module.modules.command;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.module.modules.command.listeners.MessageListener;
import de.timmi6790.discord_framework.module.modules.command.listeners.MetricListener;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
@Log4j2
public class CommandModule extends AbstractModule {
    private final Map<String, Command> commands = new CaseInsensitiveMap<>();
    private final Map<String, String> commandAliases = new CaseInsensitiveMap<>();

    private Config config;

    @Getter(AccessLevel.PUBLIC)
    private PermissionsModule permissionsModule;
    @Getter(AccessLevel.PUBLIC)
    private EventModule eventModule;
    private MetricModule metricModule;

    public CommandModule() {
        super("Command");

        this.addDiscordGatewayIntents(
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGES
        );

        this.addDependenciesAndLoadAfter(
                EventModule.class,
                ConfigModule.class,
                PermissionsModule.class
        );

        this.addLoadAfterDependencies(
                MetricModule.class
        );

        this.addDependencies(
                UserDbModule.class,
                ChannelDbModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.permissionsModule = this.getModuleOrThrow(PermissionsModule.class);
        this.eventModule = this.getModuleOrThrow(EventModule.class);
        this.metricModule = this.getModule(MetricModule.class).orElse(null);

        this.config = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());

        this.registerCommand(
                this,
                new HelpCommand(
                        this
                )
        );

        if (this.metricModule != null) {
            this.eventModule.addEventListener(
                    new MetricListener(
                            this.metricModule
                    )
            );
        }

        return true;
    }

    @Override
    public boolean onEnable() {
        if (this.config.isSetDiscordActivity()) {
            this.getDiscord().setActivity(Activity.playing(this.getMainCommand() + "help"));
        }

        // We currently need to register this during on enable, because otherwise we  have topical sort cycle.
        // This should be resolved with the dpi system
        this.getModuleOrThrow(EventModule.class).addEventListeners(
                new MessageListener(
                        this,
                        this.getModuleOrThrow(UserDbModule.class),
                        this.getModuleOrThrow(ChannelDbModule.class),
                        this.getCommand(HelpCommand.class).orElseThrow(RuntimeException::new)
                )
        );

        return true;
    }

    protected String getCommandPermissionNode(final AbstractModule module, final Command command) {
        return String.format(
                "%s.command.%s",
                module.getModuleName(),
                command.getName()
        )
                .replace(' ', '_')
                .toLowerCase();
    }

    public Optional<MetricModule> getMetricModule() {
        return Optional.ofNullable(this.metricModule);
    }

    public String getMainCommand() {
        return this.config.getMainCommand();
    }

    public long getBotId() {
        return this.getDiscordBot().getBaseShard().getSelfUser().getIdLong();
    }

    public Optional<Command> getCommand(final Class<? extends Command> commandClass) {
        for (final Command command : this.commands.values()) {
            if (command.getClass() == commandClass) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    public Optional<Command> getCommand(String commandName) {
        commandName = this.commandAliases.getOrDefault(commandName, commandName);
        return Optional.ofNullable(this.commands.get(commandName));
    }

    public Set<Command> getCommands() {
        return new HashSet<>(this.commands.values());
    }

    public Set<Command> getCommands(final Predicate<Command> commandPredicate) {
        final Set<Command> filteredCommands = new HashSet<>();

        for (final Command command : this.commands.values()) {
            if (commandPredicate.test(command)) {
                filteredCommands.add(command);
            }
        }

        return filteredCommands;
    }

    public void registerCommands(final AbstractModule module, final Command... commands) {
        for (final Command command : commands) {
            this.registerCommand(module, command);
        }
    }

    public boolean registerCommand(final AbstractModule module, final Command command) {
        if (this.commands.containsKey(command.getName())) {
            log.warn(
                    "The module {} tried to register the {} command that already exists.",
                    module.getModuleName(),
                    command.getName()
            );
            return false;
        }

        // Only set the permission id when it is the default one
        if (command.hasDefaultPermission()) {
            final String permissionNode = this.getCommandPermissionNode(module, command);
            final int permissionId = this.getPermissionsModule().addPermission(permissionNode);
            command.setPermissionId(permissionId);
        }

        log.info(
                "[{}] Registered {} command",
                module.getModuleName(),
                command.getName()
        );
        this.commands.put(command.getName(), command);
        for (final String aliasName : command.getPropertyValueOrDefault(AliasNamesProperty.class, () -> new String[0])) {
            final String existingAliasName = this.commandAliases.get(aliasName);
            if (existingAliasName == null) {
                this.commandAliases.put(aliasName, command.getName());
            } else {
                log.warn(
                        "[{}] Tried to register an already existing alias name {} for {} that is already used for the {} command",
                        module.getModuleName(),
                        aliasName,
                        command.getName(),
                        existingAliasName
                );
            }
        }

        return true;
    }
}
