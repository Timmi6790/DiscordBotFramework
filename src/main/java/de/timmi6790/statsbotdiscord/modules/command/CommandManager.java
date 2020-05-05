package de.timmi6790.statsbotdiscord.modules.command;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.events.EventMessageReceived;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager {
    private final static int COMMAND_USER_RATE_LIMIT = 10;
    private final static Permission[] MINIMUM_DISCORD_PERMISSIONS = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS};

    private final Pattern mainCommandPattern;

    @Getter
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Getter
    private final LoadingCache<Long, Short> commandSpamCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(key -> (short) 0);

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

    public boolean registerCommand(final AbstractCommand command) {
        if (this.commands.containsKey(command.getName())) {
            System.out.println(command.getName() + " is already registered");
            return false;
        }

        this.commands.put(command.getName().toLowerCase(), command);

        for (final String alias : command.getAliasNames()) {
            if (this.commandAliases.containsKey(alias)) {
                continue;
            }

            this.commandAliases.put(alias.toLowerCase(), command.getName().toLowerCase());
        }

        return true;
    }

    public void registerCommands(final AbstractCommand... commands) {
        for (final AbstractCommand command : commands) {
            this.registerCommand(command);
        }
    }

    public Optional<AbstractCommand> getCommand(final Class<? extends AbstractCommand> clazz) {
        for (final AbstractCommand command : this.commands.values()) {
            if (command.getClass().equals(clazz)) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
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
        final List<AbstractCommand> similarCommands = new ArrayList<>();

        final List<String> commandNames = new ArrayList<>();
        for (final AbstractCommand command : this.commands.values()) {
            if (!command.hasPermission(commandParameters)) {
                continue;
            }

            commandNames.add(command.getName().toLowerCase());
        }

        final String[] similarCommandNames = UtilitiesData.getSimilarityList(name, commandNames, similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarCommands.add(this.commands.get(similarCommandNames[index]));
        }

        return similarCommands;
    }

    public AbstractCommand[] getCommands() {
        return this.commands.values().toArray(new AbstractCommand[0]);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMessage(final EventMessageReceived event) {
        if (this.botId.equals(event.getAuthor().getId())) {
            return;
        }

        final GuildDb guildDb = GuildDb.getOrCreate(event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0);

        String rawMessage = event.getMessage().getContentRaw();
        boolean validStart = false;

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

        if (!validStart) {
            return;
        }

        // Spam check
        final short currentAmount = this.commandSpamCache.get(event.getAuthor().getIdLong());
        if (currentAmount > COMMAND_USER_RATE_LIMIT) {
            return;
        }

        // Server ban check
        if (guildDb.isBanned()) {
            event.getChannel().sendMessage(
                    UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                            .setTitle("Banned Server")
                            .setDescription("This server is banned from using this service.")
                            .build())
                    .delay(90, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            return;
        }

        final UserDb userDb = UserDb.getOrCreate(event.getAuthor().getIdLong());
        // User ban check
        if (userDb.isBanned()) {
            UtilitiesDiscord.sendPrivateMessage(
                    event.getAuthor(),
                    UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                            .setTitle("You are banned")
                            .setDescription("You are banned from using this service.")
            );
            return;
        }

        final EnumSet<Permission> permissions;
        if (event.isFromGuild()) {
            permissions = event.getGuild().getSelfMember().getPermissions((GuildChannel) event.getMessage().getChannel());
            // I want to have write perms in all channels the commands should work in
            for (final Permission permission : MINIMUM_DISCORD_PERMISSIONS) {
                if (!permissions.contains(permission)) {
                    UtilitiesDiscord.sendPrivateMessage(
                            event.getAuthor(),
                            UtilitiesDiscord.getDefaultEmbedBuilder(event.getAuthor(), event.getMemberOptional())
                                    .setTitle("Missing Permission")
                                    .setDescription("The bot is missing the " + MarkdownUtil.monospace(permission.getName()) + " permission.")
                    );
                    return;
                }
            }

        } else {
            permissions = EnumSet.noneOf(Permission.class);
        }

        boolean emptyArgs = false;
        String[] args = rawMessage.trim().split("\\s+");
        if (args.length == 1 && args[0].isEmpty()) {
            args = new String[]{};
            emptyArgs = true;
        }

        final ChannelDb channelDb = ChannelDb.getOrCreate(event.getChannel().getIdLong(), guildDb.getDiscordId());
        final CommandParameters commandParameters = new CommandParameters(permissions, channelDb, userDb, emptyArgs ? args : Arrays.copyOfRange(args, 1, args.length), event);

        final Optional<AbstractCommand> commandOpt = emptyArgs ? this.getCommand(HelpCommand.class) : this.getCommand(args[0]);
        final AbstractCommand command;
        if (!commandOpt.isPresent()) {
            final AbstractCommand[] similarCommands = this.getSimilarCommands(commandParameters, args[0], 0.6, 3).toArray(new AbstractCommand[0]);

            if (similarCommands.length != 0 && userDb.hasAutoCorrection()) {
                command = similarCommands[0];

            } else {
                final StringBuilder description = new StringBuilder();
                final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

                if (similarCommands.length == 0) {
                    description.append(MarkdownUtil.monospace(args[0])).append(" is not a valid command.\n");
                    description.append("Use the ").append(MarkdownUtil.bold(this.getMainCommand() + " help")).append(" command or click the ")
                            .append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all commands.");

                } else {
                    description.append(MarkdownUtil.monospace(args[0])).append(" is not a valid command.\n");
                    description.append("Is it possible that you wanted to write?\n\n");

                    for (int index = 0; similarCommands.length > index; index++) {
                        final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();
                        final AbstractCommand similarCommand = similarCommands[index];

                        description.append(emote).append(" ").append(MarkdownUtil.bold(similarCommand.getName())).append(" | ").append(similarCommand.getDescription()).append("\n");
                        emotes.put(emote, new CommandEmoteReaction(similarCommand, commandParameters));
                    }

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
                return;
            }
        } else {
            command = commandOpt.get();
        }

        // Run Command
        this.commandSpamCache.put(event.getAuthor().getIdLong(), (short) (this.commandSpamCache.get(event.getAuthor().getIdLong()) + 1));
        this.executor.execute(() -> command.runCommand(commandParameters));
    }
}
