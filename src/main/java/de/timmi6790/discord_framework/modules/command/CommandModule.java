package de.timmi6790.discord_framework.modules.command;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.commands.CommandCommand;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public class CommandModule extends AbstractModule {
    private static final String COMMAND_NAME = "commandName";
    private static final String COMMAND_DEFAULT_PERMISSION_NAME = "%s.command.%s";

    private static final String MAIN_COMMAND_PATTERN = "^((?:%s)|(?:<@[!&]%s>))([\\S\\s]*)$";

    private static final String GET_COMMAND_ID = "SELECT id FROM `command` WHERE command_name = :commandName LIMIT 1;";
    private static final String INSERT_NEW_COMMAND = "INSERT INTO command(command_name) VALUES(:commandName);";

    private static final String GET_COMMAND_CAUSE_COUNT = "SELECT COUNT(*) FROM `command_cause` WHERE cause_name = :causeName LIMIT 1;";
    private static final String INSERT_COMMAND_CAUSE = "INSERT INTO command_cause(cause_name) VALUES(:causeName);";

    private static final String GET_COMMAND_STATUS_COUNT = "SELECT COUNT(*) FROM `command_status` WHERE status_name = :statusName LIMIT 1;";
    private static final String INSERT_COMMAND_STATUS = "INSERT INTO command_status(status_name) VALUES(:statusName);";

    @Getter
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Getter
    private final LoadingCache<Long, AtomicInteger> commandSpamCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(key -> new AtomicInteger(0));
    private final Map<String, AbstractCommand<?>> commands = new HashMap<>();
    private final Map<String, String> commandAliases = new HashMap<>();
    @Getter
    private Pattern mainCommandPattern;
    @Getter
    private String mainCommand;
    @Getter
    private long botId;

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
                ChannelDbModule.class
        );
    }

    protected static Pattern compileMainCommandPattern(@NonNull final String mainCommand, final long botId) {
        return Pattern.compile(String.format(MAIN_COMMAND_PATTERN, mainCommand.replace(" ", ""), botId), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void onInitialize() {
        final Config commandConfig = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());

        this.botId = this.getDiscord().getSelfUser().getIdLong();
        this.mainCommand = commandConfig.getMainCommand();
        this.mainCommandPattern = compileMainCommandPattern(this.mainCommand, this.botId);

        this.getModuleOrThrow(EventModule.class)
                .addEventListeners(
                        new MessageListener()
                );

        this.innitDatabase();

        this.registerCommands(
                this,
                new HelpCommand(),
                new CommandCommand()
        );
    }

    @Override
    public void onEnable() {
        this.getDiscord().getPresence().setActivity(Activity.playing(this.mainCommand + "help"));
    }

    public void innitDatabase() {
        // Db
        // CommandCause
        this.getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                Arrays.stream(CommandCause.values())
                        .parallel()
                        .map(commandCause -> commandCause.name().toLowerCase())
                        .filter(nameLower ->
                                handle.createQuery(GET_COMMAND_CAUSE_COUNT)
                                        .bind("causeName", nameLower)
                                        .mapTo(int.class)
                                        .first() == 0
                        )
                        .forEach(nameLower ->
                                handle.createUpdate(INSERT_COMMAND_CAUSE)
                                        .bind("causeName", nameLower)
                                        .execute()
                        )
        );

        // CommandStatus
        this.getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                Arrays.stream(CommandResult.values())
                        .parallel()
                        .map(commandResult -> commandResult.name().toLowerCase())
                        .filter(nameLower ->
                                handle.createQuery(GET_COMMAND_STATUS_COUNT)
                                        .bind("statusName", nameLower)
                                        .mapTo(int.class)
                                        .first() == 0
                        )
                        .forEach(nameLower ->
                                handle.createUpdate(INSERT_COMMAND_STATUS)
                                        .bind("statusName", nameLower)
                                        .execute()
                        )
        );
    }

    private int getCommandDatabaseId(final AbstractCommand<?> command) {
        return this.getModuleOrThrow(DatabaseModule.class).getJdbi().withHandle(handle ->
                handle.createQuery(GET_COMMAND_ID)
                        .bind(COMMAND_NAME, command.getName())
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(INSERT_NEW_COMMAND)
                                    .bind(COMMAND_NAME, command.getName())
                                    .execute();

                            return handle.createQuery(GET_COMMAND_ID)
                                    .bind(COMMAND_NAME, command.getName())
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }

    public boolean registerCommand(final AbstractModule module, final AbstractCommand<?> command) {
        if (this.commands.containsKey(command.getName())) {
            DiscordBot.getLogger().error("{} is already registered.", command.getName());
            return false;
        }
        this.commands.put(command.getName().toLowerCase(), command);
        Arrays.stream(command.getAliasNames())
                .filter(alias -> !this.commandAliases.containsKey(alias))
                .forEach(alias -> this.commandAliases.put(alias.toLowerCase(), command.getName().toLowerCase()));

        DiscordBot.getLogger().info("Registerd {} command.", command.getName());

        command.setDbId(this.getCommandDatabaseId(command));
        final String defaultPermissionName = String.format(COMMAND_DEFAULT_PERMISSION_NAME, module.getName(), command.getName())
                .replace(" ", "_")
                .toLowerCase();
        command.setPermission(defaultPermissionName);
        command.setCommandModule(module.getClass());
        return true;
    }

    public void registerCommands(final AbstractModule module, final AbstractCommand<?>... commands) {
        Arrays.stream(commands)
                .parallel()
                .forEach(command -> this.registerCommand(module, command));
    }

    public Optional<AbstractCommand<?>> getCommand(final Class<? extends AbstractCommand<?>> clazz) {
        return this.commands.values().stream()
                .filter(command -> command.getClass().equals(clazz))
                .findAny();
    }


    public Optional<AbstractCommand<?>> getCommand(String name) {
        name = this.commandAliases.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.commands.get(name));
    }

    public List<AbstractCommand<?>> getSimilarCommands(final CommandParameters commandParameters, final String name, final double similarity, final int limit) {
        return DataUtilities.getSimilarityList(
                name,
                this.commands.values()
                        .stream()
                        .filter(command -> command.hasPermission(commandParameters))
                        .collect(Collectors.toList()),
                AbstractCommand::getName,
                similarity,
                limit
        );
    }

    public Collection<AbstractCommand<?>> getCommands() {
        return this.commands.values();
    }
}
