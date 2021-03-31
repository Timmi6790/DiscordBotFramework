package de.timmi6790.discord_framework.module.modules.command;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.module.modules.command.listeners.CommandLoggingListener;
import de.timmi6790.discord_framework.module.modules.command.listeners.MessageListener;
import de.timmi6790.discord_framework.module.modules.command.repository.CommandRepository;
import de.timmi6790.discord_framework.module.modules.command.repository.mysql.CommandRepositoryMysql;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import io.github.bucket4j.Bucket;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Getter
@Log4j2
public class CommandModule extends AbstractModule {
    private static final String MAIN_COMMAND_PATTERN = "^(?:(?:%s)|(?:<@[!&]%s>))([\\S\\s]*)$";

    private final RateLimitService commandRateLimitService = new RateLimitService();

    private final Map<String, AbstractCommand> commands = new CaseInsensitiveMap<>();
    private final Map<String, String> commandAliases = new CaseInsensitiveMap<>();

    private Pattern mainCommandPattern;
    private String mainCommand;
    private long botId;

    private CommandRepository commandRepository;
    private Config commandConfig;

    public CommandModule() {
        super("Command");

        this.addDiscordGatewayIntents(
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGES
        );

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                EventModule.class,
                PermissionsModule.class,
                DatabaseModule.class
        );

        this.addDependencies(
                UserDbModule.class,
                GuildDbModule.class,
                ChannelDbModule.class,
                RankModule.class
        );
    }

    public static Pattern compileMainCommandPattern(@NonNull final String mainCommand, final long botId) {
        return Pattern.compile(
                String.format(MAIN_COMMAND_PATTERN, mainCommand.replace(" ", ""), botId),
                Pattern.CASE_INSENSITIVE
        );
    }

    @Override
    public boolean onInitialize() {
        this.commandRepository = new CommandRepositoryMysql(this.getModuleOrThrow(DatabaseModule.class).getJdbi());
        this.commandConfig = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());

        this.botId = this.getDiscordBot().getBaseShard().getSelfUser().getIdLong();
        this.mainCommand = this.commandConfig.getMainCommand();
        this.mainCommandPattern = compileMainCommandPattern(this.mainCommand, this.botId);

        this.commandRepository.init(CommandCause.values(), CommandResult.values());

        final HelpCommand helpCommand = new HelpCommand();
        this.registerCommands(
                this,
                helpCommand
        );

        this.getModuleOrThrow(EventModule.class)
                .addEventListeners(
                        new MessageListener(
                                this,
                                this.getModuleOrThrow(GuildDbModule.class),
                                helpCommand
                        ),
                        new CommandLoggingListener(this.commandRepository)
                );
        return true;
    }

    @Override
    public boolean onEnable() {
        if (this.commandConfig.isSetDiscordActivity()) {
            this.getDiscord().setActivity(Activity.playing(this.mainCommand + "help"));
        }
        return true;
    }

    private int getCommandDatabaseId(@NonNull final AbstractCommand command) {
        return this.commandRepository.getCommandDatabaseId(command);
    }

    public void registerCommands(@NonNull final AbstractModule module, final AbstractCommand... commands) {
        for (final AbstractCommand command : commands) {
            this.registerCommand(module, command);
        }
    }

    public boolean registerCommand(@NonNull final AbstractModule module, @NonNull final AbstractCommand command) {
        if (this.commands.containsKey(command.getName())) {
            log.error("{} is already registered.", command.getName());
            return false;
        }

        log.info("Registerd {} command.", command.getName());
        this.commands.put(command.getName(), command);
        for (final String aliasName : command.getAliasNames()) {
            if (this.commandAliases.containsKey(aliasName)) {
                log.warn(
                        "Can't register alias name {} for {}, it already exists.",
                        aliasName,
                        command.getName()
                );
                continue;
            }

            this.commandAliases.put(aliasName, command.getName());
        }

        if (command.getDbId() == -1) {
            command.setDbId(this.getCommandDatabaseId(command));
        }
        if (command.getPermissionId() == -1) {
            final String defaultPermissionName = String.format("%s.command.%s", module.getModuleName(), command.getName())
                    .replace(' ', '_')
                    .toLowerCase();
            command.setPermission(defaultPermissionName);
        }
        command.setRegisteredModule(module.getClass());
        return true;
    }

    public Optional<AbstractCommand> getCommand(@NonNull final Class<? extends AbstractCommand> clazz) {
        for (final AbstractCommand command : this.commands.values()) {
            if (command.getClass().equals(clazz)) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
    }


    public Optional<AbstractCommand> getCommand(@NonNull String name) {
        name = this.commandAliases.getOrDefault(name, name);
        return Optional.ofNullable(this.commands.get(name));
    }

    public List<AbstractCommand> getCommandsWithPerms(@NonNull final CommandParameters commandParameters) {
        final List<AbstractCommand> foundCommands = new ArrayList<>();
        for (final AbstractCommand command : this.getCommands()) {
            if (command.hasPermission(commandParameters)) {
                foundCommands.add(command);
            }
        }
        return foundCommands;
    }

    public List<AbstractCommand> getCommands() {
        return new ArrayList<>(this.commands.values());
    }

    public Bucket resolveRateBucket(final long userId) {
        return this.commandRateLimitService.resolveBucket(userId);
    }
}
