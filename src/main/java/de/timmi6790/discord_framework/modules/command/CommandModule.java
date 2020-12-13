package de.timmi6790.discord_framework.modules.command;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.command.listeners.CommandLoggingListener;
import de.timmi6790.discord_framework.modules.command.listeners.MessageListener;
import de.timmi6790.discord_framework.modules.command.repository.CommandRepository;
import de.timmi6790.discord_framework.modules.command.repository.CommandRepositoryMysql;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CommandModule extends AbstractModule {
    private static final String MAIN_COMMAND_PATTERN = "^(?:(?:%s)|(?:<@[!&]%s>))([\\S\\s]*)$";
    @Getter
    private static final int COMMAND_USER_RATE_LIMIT = 10;

    private final LoadingCache<Long, AtomicInteger> commandSpamCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(key -> new AtomicInteger(0));

    private final Map<String, AbstractCommand> commands = new CaseInsensitiveMap<>();
    private final Map<String, String> commandAliases = new CaseInsensitiveMap<>();
    private Pattern mainCommandPattern;
    private String mainCommand;
    private long botId;

    private CommandRepository commandRepository;
    private Config commandConfig;

    public CommandModule() {
        super("Command");

        this.addGatewayIntents(
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
    public void onInitialize() {
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
    }

    @Override
    public void onEnable() {
        if (this.commandConfig.isSetDiscordActivity()) {
            this.getDiscord().setActivity(Activity.playing(this.mainCommand + "help"));
        }
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
            DiscordBot.getLogger().error("{} is already registered.", command.getName());
            return false;
        }

        DiscordBot.getLogger().info("Registerd {} command.", command.getName());
        this.commands.put(command.getName(), command);
        for (final String aliasName : command.getAliasNames()) {
            if (this.commandAliases.containsKey(aliasName)) {
                DiscordBot.getLogger().warn(
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
            final String defaultPermissionName = String.format("%s.command.%s", module.getName(), command.getName())
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
}
