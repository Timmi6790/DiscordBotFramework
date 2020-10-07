package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.modules.command.listeners.CommandLoggingListener;
import de.timmi6790.discord_framework.modules.command.listeners.MessageListener;
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
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jdbi.v3.core.Jdbi;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public class CommandModule extends AbstractModule {
    private static final String COMMAND_NAME = "commandName";

    private static final String MAIN_COMMAND_PATTERN = "^(?:(?:%s)|(?:<@[!&]%s>))([\\S\\s]*)$";

    private static final String GET_COMMAND_ID = "SELECT id FROM `command` WHERE command_name = :commandName LIMIT 1;";
    private static final String INSERT_NEW_COMMAND = "INSERT INTO command(command_name) VALUES(:commandName);";

    private static final String GET_COMMAND_CAUSE_COUNT = "SELECT COUNT(*) FROM `command_cause` WHERE cause_name = :causeName LIMIT 1;";
    private static final String INSERT_COMMAND_CAUSE = "INSERT INTO command_cause(cause_name) VALUES(:causeName);";

    private static final String GET_COMMAND_STATUS_COUNT = "SELECT COUNT(*) FROM `command_status` WHERE status_name = :statusName LIMIT 1;";
    private static final String INSERT_COMMAND_STATUS = "INSERT INTO command_status(status_name) VALUES(:statusName);";

    private final Map<String, AbstractCommand> commands = new CaseInsensitiveMap<>();
    private final Map<String, String> commandAliases = new CaseInsensitiveMap<>();
    @Getter
    private Pattern mainCommandPattern;
    @Getter
    private String mainCommand;
    @Getter
    private long botId;

    private Jdbi database;
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
                ChannelDbModule.class
        );
    }

    public static Pattern compileMainCommandPattern(@NonNull final String mainCommand, final long botId) {
        return Pattern.compile(String.format(MAIN_COMMAND_PATTERN, mainCommand.replace(" ", ""), botId), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void onInitialize() {
        this.database = this.getModuleOrThrow(DatabaseModule.class).getJdbi();
        this.commandConfig = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());

        this.botId = this.getDiscord().getSelfUser().getIdLong();
        this.mainCommand = this.commandConfig.getMainCommand();
        this.mainCommandPattern = compileMainCommandPattern(this.mainCommand, this.botId);

        this.innitDatabase();

        final HelpCommand helpCommand = new HelpCommand(this);
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
                        new CommandLoggingListener(this.database)
                );
    }

    @Override
    public void onEnable() {
        if (this.commandConfig.isSetDiscordActivity()) {
            this.getDiscord().getPresence().setActivity(Activity.playing(this.mainCommand + "help"));
        }
    }

    public void innitDatabase() {
        // Db
        // CommandCause
        this.database.useHandle(handle ->
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
        this.database.useHandle(handle ->
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

    private int getCommandDatabaseId(@NonNull final AbstractCommand command) {
        return this.database.withHandle(handle ->
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
                DiscordBot.getLogger().warn("Can't register alias name {} for {}, it already exists.", aliasName, command.getName());
                continue;
            }

            this.commandAliases.put(aliasName, command.getName());
        }

        if (command.getDbId() == -1) {
            command.setDbId(this.getCommandDatabaseId(command));
        }
        if (command.getPermissionId() == -1) {
            final String defaultPermissionName = String.format("%s.command.%s", module.getName(), command.getName())
                    .replace(" ", "_")
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

    public List<AbstractCommand> getSimilarCommands(@NonNull final CommandParameters commandParameters,
                                                    @NonNull final String name,
                                                    final double similarity,
                                                    final int limit) {
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

    public Collection<AbstractCommand> getCommands() {
        return this.commands.values();
    }
}
