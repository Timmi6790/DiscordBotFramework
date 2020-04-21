package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.AbstractStatsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class AbstractJavaStatsCommand extends AbstractStatsCommand {
    private final static Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
    private final static List<String> STATS_TIME = new ArrayList<>(Arrays.asList("TimeInGame", "TimeInHub", "TimePlaying"));
    private final static DecimalFormat FORMAT_NUMBER = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private final static Cache<UUID, BufferedImage> SKIN_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    static {
        final DecimalFormatSymbols symbols = FORMAT_NUMBER.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        FORMAT_NUMBER.setDecimalFormatSymbols(symbols);
    }

    public AbstractJavaStatsCommand(final String name, final String category, final String description, final String syntax, final String... aliasNames) {
        super(name, category, description, syntax, aliasNames);
    }

    protected String getFormattedScore(final JavaStat stat, final long score) {
        if (STATS_TIME.contains(stat.getName())) {
            return this.getFormattedTime(score);
        }

        return FORMAT_NUMBER.format(score);
    }

    protected JavaGame getGame(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        final Optional<JavaGame> game = ((MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class)).getJavaGame(name);
        if (game.isPresent()) {
            return game.get();
        }

        final JavaGame[] similarGames = ((MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class)).getSimilarGames(name, 0.6, 3).toArray(new JavaGame[0]);

        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder();
        description.append(MarkdownUtil.monospace(name)).append(" is not a valid game.\n");

        if (similarGames.length == 0) {
            description.append("Use the ").append(MarkdownUtil.bold(StatsBot.getCommandManager().getMainCommand() + " games"))
                    .append(" command or click the ").append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all games.");

        } else {
            description.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarGames.length > index; index++) {
                final JavaGame similarGame = similarGames[index];
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(emote).append(" ").append(MarkdownUtil.bold(similarGame.getName())).append(" | ").append(similarGame.getDescription()).append("\n");

                final CommandParameters newCommandParameters = commandParameters.clone();
                newCommandParameters.getArgs()[argPos] = similarGame.getName();
                emotes.put(emote, new CommandEmoteReaction(this, newCommandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(" All games");
        }

        StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).ifPresent(gamesCommand -> {
                    final CommandParameters newCommandParameters = commandParameters.clone();
                    newCommandParameters.setArgs(new String[0]);

                    emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(gamesCommand, newCommandParameters));
                }
        );

        commandParameters.getEvent().getChannel().sendMessage(
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Invalid Game")
                        .setDescription(description)
                        .build())
                .queue(message -> {
                    if (!emotes.isEmpty()) {
                        final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, commandParameters.getEvent().getAuthor().getIdLong(),
                                commandParameters.getEvent().getChannel().getIdLong());
                        StatsBot.getEmoteReactionManager().addEmoteReactionMessage(message, emoteReactionMessage);
                    }

                    message.delete().queueAfter(90, TimeUnit.SECONDS);
                });


        throw new CommandReturnException();
    }

    protected JavaStat getStat(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaStat> stat = game.getStat(name);
        if (stat.isPresent()) {
            return stat.get();
        }

        final JavaStat[] similarStats = game.getSimilarStats(name, 0.6, 3).toArray(new JavaStat[0]);

        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder();
        description.append(MarkdownUtil.monospace(name)).append(" is not a valid stat for ").append(MarkdownUtil.bold(game.getName())).append(".\n");

        if (similarStats.length == 0) {
            description.append("Use the ").append(MarkdownUtil.bold(StatsBot.getCommandManager().getMainCommand() + " games " + game.getName()))
                    .append(" command or click the ").append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all stats.");

        } else {
            description.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarStats.length > index; index++) {
                final JavaStat similarStat = similarStats[index];
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(emote).append(" ").append(MarkdownUtil.bold(similarStat.getName())).append(" | ").append(similarStat.getDescription()).append("\n");

                final CommandParameters newCommandParameters = commandParameters.clone();
                newCommandParameters.getArgs()[argPos] = similarStat.getName();
                emotes.put(emote, new CommandEmoteReaction(this, newCommandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(" All stats");
        }

        StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).ifPresent(gamesCommand -> {
                    final CommandParameters newCommandParameters = commandParameters.clone();
                    newCommandParameters.setArgs(new String[]{game.getName()});

                    emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(gamesCommand, newCommandParameters));
                }
        );

        commandParameters.getEvent().getChannel().sendMessage(
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Invalid Stat")
                        .setDescription(description)
                        .build())
                .queue(message -> {
                    if (!emotes.isEmpty()) {
                        final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, commandParameters.getEvent().getAuthor().getIdLong(),
                                commandParameters.getEvent().getChannel().getIdLong());
                        StatsBot.getEmoteReactionManager().addEmoteReactionMessage(message, emoteReactionMessage);
                    }

                    message.delete().queueAfter(90, TimeUnit.SECONDS);
                });


        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name;
        if (argPos >= commandParameters.getArgs().length) {
            name = "All";
        } else {
            name = commandParameters.getArgs()[argPos];
        }

        for (final JavaStat stat : game.getStats().values()) {
            if (stat.getBoard(name).isPresent()) {
                return stat.getBoard(name).get();
            }
        }

        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String name;
        if (argPos >= commandParameters.getArgs().length) {
            name = "All";
        } else {
            name = commandParameters.getArgs()[argPos];
        }
        final Optional<JavaBoard> board = stat.getBoard(name);
        if (board.isPresent()) {
            return board.get();
        }

        final List<JavaBoard> similarBoards = stat.getSimilarBoard(name, 0.6, 3);


        throw new CommandReturnException();
    }

    protected String getPlayer(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        if (NAME_PATTERN.matcher(name).find()) {
            return name;
        }

        throw new CommandReturnException(
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a minecraft name.")
        );
    }

    public CompletableFuture<BufferedImage> getPlayerSkin(final UUID uuid) {
        final CompletableFuture<BufferedImage> completableFuture = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().submit(() -> {
            final BufferedImage skin = SKIN_CACHE.getIfPresent(uuid);
            if (skin != null) {
                completableFuture.complete(skin);
                return;
            }

            final HttpResponse<byte[]> response = Unirest.get("https://visage.surgeplay.com/frontfull/" + uuid.toString().replace("-", "") + ".png")
                    .connectTimeout(6_000)
                    .asBytes();

            if (!response.isSuccess()) {
                completableFuture.complete(null);
                return;
            }

            try {
                final InputStream in = new ByteArrayInputStream(response.getBody());

                final BufferedImage image = ImageIO.read(in);
                SKIN_CACHE.put(uuid, image);

                completableFuture.complete(image);
                return;
            } catch (final IOException e) {
                e.printStackTrace();
            }

            completableFuture.complete(null);
        });

        return completableFuture;
    }
}
