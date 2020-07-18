package de.timmi6790.statsbotdiscord.modules.command;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.statsbotdiscord.events.MessageReceivedIntEvent;
import de.timmi6790.statsbotdiscord.modules.core.ChannelDb;
import de.timmi6790.statsbotdiscord.modules.core.GuildDb;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.HelpCommand;
import de.timmi6790.statsbotdiscord.modules.eventhandler.EventPriority;
import de.timmi6790.statsbotdiscord.modules.eventhandler.SubscribeEvent;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import de.timmi6790.statsbotdiscord.utilities.discord.UtilitiesDiscordMessages;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.jdbi.v3.core.Jdbi;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandManager {
    private static final String MAIN_COMMAND_PATTERN = "^(?:(?:%s)|(?:<@[!&]%s>))(.*)$";
    private static final Pattern MESSAGE_SPLIT_PATTERN = Pattern.compile("\\s+");

    private static final int COMMAND_USER_RATE_LIMIT = 10;
    private static final List<Permission> MINIMUM_DISCORD_PERMISSIONS = Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS);

    private static final String GET_COMMAND_CAUSE_COUNT = "SELECT COUNT(*) FROM `command_cause` WHERE cause_name = :causeName LIMIT 1;";
    private static final String INSERT_COMMAND_CAUSE = "INSERT INTO command_cause(cause_name) VALUES(:causeName);";

    private static final String GET_COMMAND_STATUS_COUNT = "SELECT COUNT(*) FROM `command_status` WHERE status_name = :statusName LIMIT 1;";
    private static final String INSERT_COMMAND_STATUS = "INSERT INTO command_status(status_name) VALUES(:statusName);";

    @Getter
    private final Pattern mainCommandPattern;

    @Getter
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Getter
    private final LoadingCache<Long, AtomicInteger> commandSpamCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(key -> new AtomicInteger(0));

    @Getter
    private final String mainCommand;
    private final long botId;

    private final Map<String, AbstractCommand> commands = new HashMap<>();
    private final Map<String, String> commandAliases = new HashMap<>();

    public CommandManager(final String mainCommand, final long botId) {
        this.botId = botId;

        this.mainCommandPattern = Pattern.compile(String.format(MAIN_COMMAND_PATTERN, mainCommand, this.botId), Pattern.CASE_INSENSITIVE);
        this.mainCommand = mainCommand;
    }

    public void innitDatabase(final Jdbi database) {
        // Db
        // CommandCause
        database.useHandle(handle ->
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
        database.useHandle(handle ->
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

    public boolean registerCommand(final AbstractCommand command) {
        if (this.commands.containsKey(command.getName())) {
            System.out.println(command.getName() + " is already registered");
            return false;
        }

        this.commands.put(command.getName().toLowerCase(), command);
        Arrays.stream(command.getAliasNames())
                .filter(alias -> !this.commandAliases.containsKey(alias))
                .forEach(alias -> this.commandAliases.put(alias.toLowerCase(), command.getName().toLowerCase()));
        return true;
    }

    public void registerCommands(final AbstractCommand... commands) {
        Arrays.stream(commands).forEach(this::registerCommand);
    }

    public Optional<AbstractCommand> getCommand(final Class<? extends AbstractCommand> clazz) {
        return this.commands.values().stream()
                .filter(command -> command.getClass().equals(clazz))
                .findAny();
    }


    public Optional<AbstractCommand> getCommand(String name) {
        name = this.commandAliases.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.commands.get(name));
    }

    public List<AbstractCommand> getSimilarCommands(final CommandParameters commandParameters, final String name, final double similarity, final int limit) {
        return UtilitiesData.getSimilarityList(
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMessage(final MessageReceivedIntEvent event) {
        // Ignore yourself
        if (this.botId == event.getAuthor().getIdLong()) {
            return;
        }

        final GuildDb guildDb = GuildDb.getOrCreate(event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0);

        String rawMessage = event.getMessage().getContentRaw();
        boolean validStart = false;

        // Check if the message matches the main or guild specific start regex
        final Matcher mainMatcher = this.mainCommandPattern.matcher(rawMessage);
        if (mainMatcher.find()) {
            validStart = true;
            rawMessage = mainMatcher.group(1).trim();

        } else if (guildDb.getCommandAliasPattern().isPresent()) {
            final Matcher guildAliasMatcher = guildDb.getCommandAliasPattern().get().matcher(rawMessage);
            if (guildAliasMatcher.find()) {
                validStart = true;
                rawMessage = guildAliasMatcher.group(1).trim();
            }
        }

        // Invalid start or spam protection
        if (!validStart || this.commandSpamCache.get(event.getAuthor().getIdLong()).get() > COMMAND_USER_RATE_LIMIT) {
            return;
        }

        // Server ban check
        if (guildDb.isBanned()) {
            UtilitiesDiscordMessages.sendGuildBanMessage(event);
            return;
        }

        final UserDb userDb = UserDb.getOrCreate(event.getAuthor().getIdLong());
        // User ban check
        if (userDb.isBanned()) {
            UtilitiesDiscordMessages.sendUserBanMessage(event);
            return;
        }

        // I want to have write perms in all channels the commands should work in
        if (event.isFromGuild()) {
            final EnumSet<Permission> permissions = event.getGuild().getSelfMember().getPermissions((GuildChannel) event.getMessage().getChannel());
            final List<Permission> missingPerms = MINIMUM_DISCORD_PERMISSIONS.stream()
                    .filter(perm -> !permissions.contains(perm))
                    .collect(Collectors.toList());
            if (!missingPerms.isEmpty()) {
                UtilitiesDiscordMessages.sendMissingPermsMessage(event, missingPerms);
                return;
            }
        }

        final String[] args = rawMessage.isEmpty() ? new String[0] : MESSAGE_SPLIT_PATTERN.split(rawMessage);
        final CommandParameters commandParameters = new CommandParameters(
                event.isFromGuild() ? event.getGuild().getSelfMember().getPermissions((GuildChannel) event.getMessage().getChannel()) : EnumSet.noneOf(Permission.class),
                ChannelDb.getOrCreate(event.getChannel().getIdLong(), guildDb.getDiscordId()),
                userDb,
                event,
                args.length == 0 ? args : Arrays.copyOfRange(args, 1, args.length)
        );

        final Optional<AbstractCommand> commandOpt = args.length == 0 ? this.getCommand(HelpCommand.class) : this.getCommand(args[0]);
        final AbstractCommand command;
        if (commandOpt.isPresent()) {
            command = commandOpt.get();
        } else {
            final List<AbstractCommand> similarCommands = this.getSimilarCommands(commandParameters, args[0], 0.6, 3);
            if (!similarCommands.isEmpty() && userDb.hasAutoCorrection()) {
                command = similarCommands.get(0);

            } else {
                UtilitiesDiscordMessages.sendIncorrectCommandHelpMessage(event, similarCommands, args[0], commandParameters);
                return;
            }
        }

        // Run Command
        this.commandSpamCache.get(event.getAuthor().getIdLong()).incrementAndGet();
        this.executor.execute(() -> command.runCommand(commandParameters, CommandCause.USER));
    }
}
