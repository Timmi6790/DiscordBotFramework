package de.timmi6790.statsbotdiscord.modules.command;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.events.MessageReceivedIntEvent;
import de.timmi6790.statsbotdiscord.modules.core.ChannelDb;
import de.timmi6790.statsbotdiscord.modules.core.GuildDb;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.HelpCommand;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.eventhandler.EventPriority;
import de.timmi6790.statsbotdiscord.modules.eventhandler.SubscribeEvent;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommandManager {
    private final static int COMMAND_USER_RATE_LIMIT = 10;
    private final static List<Permission> MINIMUM_DISCORD_PERMISSIONS = Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS);

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
    private final String botId;

    private final Map<String, AbstractCommand> commands = new HashMap<>();
    private final Map<String, String> commandAliases = new HashMap<>();

    public CommandManager(final String mainCommand) {
        this.botId = StatsBot.getDiscord().getSelfUser().getId();
        this.mainCommandPattern = Pattern.compile("^((" + mainCommand + ")|(<@[!&]" + this.botId + ">))", Pattern.CASE_INSENSITIVE);

        this.mainCommand = mainCommand;

        StatsBot.getEventManager().addEventListener(this);
    }

    private void sendMissingPermsMessage(final MessageReceivedIntEvent event, final List<Permission> missingPerms) {
        final String perms = missingPerms.stream()
                .map(perm -> MarkdownUtil.monospace(perm.getName()))
                .collect(Collectors.joining(","));

        UtilitiesDiscord.sendPrivateMessage(
                event.getAuthor(),
                UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("Missing Permission")
                        .setDescription("The bot is missing " + perms + " permission(s).")
        );
    }

    private void sendUserBanMessage(final MessageReceivedIntEvent event) {
        UtilitiesDiscord.sendPrivateMessage(
                event.getAuthor(),
                UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("You are banned")
                        .setDescription("You are banned from using this service.")
        );
    }

    private void sendGuildBanMessage(final MessageReceivedIntEvent event) {
        event.getChannel().sendMessage(
                UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("Banned Server")
                        .setDescription("This server is banned from using this service.")
                        .build())
                .delay(90, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();
    }

    private void sendInvalidCommandMessage(final MessageReceivedIntEvent event, final List<AbstractCommand> similarCommands, final String firstArg, final CommandParameters commandParameters) {
        final StringBuilder description = new StringBuilder();
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        if (similarCommands.isEmpty()) {
            description.append(MarkdownUtil.monospace(firstArg)).append(" is not a valid command.\n")
                    .append("Use the ").append(MarkdownUtil.bold(this.getMainCommand() + " help")).append(" command or click the ")
                    .append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all commands.");

        } else {
            description.append(MarkdownUtil.monospace(firstArg)).append(" is not a valid command.\n")
                    .append("Is it possible that you wanted to write?\n\n");

            IntStream.range(0, similarCommands.size())
                    .forEach(index -> {
                        final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();
                        final AbstractCommand similarCommand = similarCommands.get(index);

                        description.append(emote).append(" ").append(MarkdownUtil.bold(similarCommand.getName())).append(" | ").append(similarCommand.getDescription()).append("\n");
                        emotes.put(emote, new CommandEmoteReaction(similarCommand, commandParameters));
                    });

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(" All commands");
        }
        this.getCommand(HelpCommand.class).ifPresent(helpCommand -> emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(helpCommand, commandParameters)));

        final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, event.getAuthor().getIdLong(), event.getChannel().getIdLong());
        event.getChannel().sendMessage(
                UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                        .setTitle("Invalid Command")
                        .setDescription(description)
                        .setFooter("↓ Click Me!")
                        .build())
                .queue(sendMessage -> {
                            StatsBot.getEmoteReactionManager().addEmoteReactionMessage(sendMessage, emoteReactionMessage);
                            sendMessage.delete().queueAfter(90, TimeUnit.SECONDS);
                        }
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


    public Optional<AbstractCommand> getCommand(final String name) {
        final AbstractCommand command = this.commands.get(name.toLowerCase());
        if (command != null) {
            return Optional.of(command);
        }

        final String commandAlias = this.commandAliases.get(name.toLowerCase());
        if (commandAlias == null) {
            return Optional.empty();
        }

        return Optional.of(this.commands.get(commandAlias));
    }

    public List<AbstractCommand> getSimilarCommands(final CommandParameters commandParameters, final String name, final double similarity, final int limit) {
        return UtilitiesData.getSimilarityList(
                name,
                this.commands.values()
                        .stream()
                        .filter(command -> command.hasPermission(commandParameters))
                        .map(command -> command.getName().toLowerCase())
                        .collect(Collectors.toList()),
                similarity,
                limit)
                .stream()
                .map(this.commands::get)
                .collect(Collectors.toList());
    }

    public Collection<AbstractCommand> getCommands() {
        return this.commands.values();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMessage(final MessageReceivedIntEvent event) {
        // Ignore yourself
        if (this.botId.equals(event.getAuthor().getId())) {
            return;
        }

        final GuildDb guildDb = GuildDb.getOrCreate(event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0);

        String rawMessage = event.getMessage().getContentRaw();
        boolean validStart = false;

        // Check if the message matches the main or guild specific start regex
        final Matcher mainMatcher = this.mainCommandPattern.matcher(rawMessage);
        if (mainMatcher.find()) {
            validStart = true;
            rawMessage = rawMessage.substring(mainMatcher.group(1).length());

        } else if (guildDb.getCommandAliasPattern() != null) {
            final Matcher guildAliasMatcher = guildDb.getCommandAliasPattern().matcher(rawMessage);
            if (guildAliasMatcher.find()) {
                validStart = true;
                rawMessage = rawMessage.substring(guildAliasMatcher.group(1).length());
            }
        }

        // Invalid start or spam protection
        if (!validStart || this.commandSpamCache.get(event.getAuthor().getIdLong()).get() > COMMAND_USER_RATE_LIMIT) {
            return;
        }

        // Server ban check
        if (guildDb.isBanned()) {
            this.sendGuildBanMessage(event);
            return;
        }

        final UserDb userDb = UserDb.getOrCreate(event.getAuthor().getIdLong());
        // User ban check
        if (userDb.isBanned()) {
            this.sendUserBanMessage(event);
            return;
        }

        final EnumSet<Permission> permissions;
        if (event.isFromGuild()) {
            // I want to have write perms in all channels the commands should work in
            permissions = event.getGuild().getSelfMember().getPermissions((GuildChannel) event.getMessage().getChannel());
            final List<Permission> missingPerms = MINIMUM_DISCORD_PERMISSIONS.stream()
                    .filter(perm -> !permissions.contains(perm))
                    .collect(Collectors.toList());
            if (!missingPerms.isEmpty()) {
                this.sendMissingPermsMessage(event, missingPerms);
                return;
            }

        } else {
            // Private chat
            permissions = EnumSet.noneOf(Permission.class);
        }

        boolean emptyArgs = false;
        String[] args = rawMessage.trim().split("\\s+");
        if (args.length == 1 && args[0].isEmpty()) {
            args = new String[]{};
            emptyArgs = true;
        }

        final CommandParameters commandParameters = new CommandParameters(
                permissions,
                ChannelDb.getOrCreate(event.getChannel().getIdLong(), guildDb.getDiscordId()),
                userDb,
                emptyArgs ? args : Arrays.copyOfRange(args, 1, args.length),
                event
        );

        final Optional<AbstractCommand> commandOpt = emptyArgs ? this.getCommand(HelpCommand.class) : this.getCommand(args[0]);
        final AbstractCommand command;
        if (!commandOpt.isPresent()) {
            final List<AbstractCommand> similarCommands = this.getSimilarCommands(commandParameters, args[0], 0.6, 3);
            if (!similarCommands.isEmpty() && userDb.hasAutoCorrection()) {
                command = similarCommands.get(0);

            } else {
                this.sendInvalidCommandMessage(event, similarCommands, args[0], commandParameters);
                return;
            }
        } else {
            command = commandOpt.get();
        }

        // Run Command
        this.commandSpamCache.get(event.getAuthor().getIdLong()).incrementAndGet();
        this.executor.execute(() -> command.runCommand(commandParameters));
    }
}
