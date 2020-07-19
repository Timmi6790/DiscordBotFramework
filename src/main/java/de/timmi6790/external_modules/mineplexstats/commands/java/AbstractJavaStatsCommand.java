package de.timmi6790.external_modules.mineplexstats.commands.java;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.utilities.UtilitiesData;
import de.timmi6790.external_modules.mineplexstats.commands.AbstractStatsCommand;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGroup;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaStat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractJavaStatsCommand extends AbstractStatsCommand {
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{1,16}$");
    private static final List<String> STATS_TIME = new ArrayList<>(Arrays.asList("Ingame Time", "Hub Time", "Time Playing"));

    private static final Cache<UUID, BufferedImage> SKIN_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    public AbstractJavaStatsCommand(final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, "MineplexStats - Java", description, syntax, aliasNames);
    }

    protected String getFormattedScore(final JavaStat stat, final long score) {
        if (STATS_TIME.contains(stat.getName())) {
            return this.getFormattedTime(score);
        }

        return this.getFormattedNumber(score);
    }

    protected CompletableFuture<BufferedImage> getPlayerSkin(final UUID uuid) {
        final CompletableFuture<BufferedImage> completableFuture = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().submit(() -> {
            final BufferedImage skin = SKIN_CACHE.getIfPresent(uuid);
            if (skin != null) {
                completableFuture.complete(skin);
                return;
            }

            final HttpResponse<byte[]> response = Unirest.get("https://visage.surgeplay.com/frontfull/" + uuid.toString().replace("-", "") + ".png")
                    .connectTimeout(10_000)
                    .asBytes();

            if (!response.isSuccess()) {
                completableFuture.complete(null);
                return;
            }

            try (final InputStream in = new ByteArrayInputStream(response.getBody())) {
                final BufferedImage image = ImageIO.read(in);

                completableFuture.complete(image);
                SKIN_CACHE.put(uuid, image);
            } catch (final IOException e) {
                e.printStackTrace();
                completableFuture.complete(null);
            }
        });

        return completableFuture;
    }

    // Arg Parsing
    protected JavaGame getGame(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaGame> game = this.getStatsModule().getJavaGame(name);
        if (game.isPresent()) {
            return game.get();
        }

        final List<JavaGame> similarGames = this.getStatsModule().getSimilarJavaGames(name, 0.6, 3);
        if (!similarGames.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarGames.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "game",
                DiscordBot.getCommandManager()
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[0],
                similarGames.stream()
                        .map(JavaGame::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected JavaStat getStat(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaStat> stat = game.getStat(name);
        if (stat.isPresent()) {
            return stat.get();
        }

        final List<JavaStat> similarStats = game.getSimilarStats(JavaGame.getCleanStat(name), 0.6, 3);
        if (!similarStats.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarStats.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "stat",
                DiscordBot.getCommandManager()
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[]{game.getName()},
                similarStats.stream()
                        .map(JavaStat::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected JavaStat getStat(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaStat> stat = this.getStatsModule().getJavaGames().values()
                .stream()
                .map(game -> game.getStat(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingInt(javaStat -> javaStat.getBoards().size()));
        if (stat.isPresent()) {
            return stat.get();
        }

        // TODO: Add error message
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Stat")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a valid stat. " +
                                "\nTODO: Add help emotes." +
                                "\n In the meantime just use any valid stat name, if that is not working scream at me."),
                90
        );
        /*
        final List<JavaStat> similarStats = game.getSimilarStats(JavaGame.getCleanStat(name), 0.6, 3);
        if (!similarStats.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarStats.get(0);
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "stat", command, new String[]{game.getName()}, similarStats.stream().map(JavaStat::getName).collect(Collectors.toList()));
         */
        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = argPos >= commandParameters.getArgs().length ? "All" : commandParameters.getArgs()[argPos];

        for (final JavaStat stat : game.getStats().values()) {
            if (stat.getBoard(name).isPresent()) {
                return stat.getBoard(name).get();
            }
        }

        final List<String> similarBoards = UtilitiesData.getSimilarityList(
                name,
                game.getStats().values()
                        .stream()
                        .flatMap(stat -> stat.getBoardNames().stream())
                        .collect(Collectors.toSet()),
                0.0,
                6
        );
        if (!similarBoards.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            final String newBoard = similarBoards.get(0);
            final Optional<JavaBoard> similarBoard = game.getStats().values().stream()
                    .flatMap(stat -> stat.getBoards().values().stream())
                    .filter(board -> board.getName().equalsIgnoreCase(newBoard))
                    .findAny();

            if (similarBoard.isPresent()) {
                return similarBoard.get();
            }
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "board",
                DiscordBot.getCommandManager()
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[]{game.getName(), game.getStats().values().stream().findFirst().map(JavaStat::getName).orElse("")},
                similarBoards
        );
        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String name = argPos >= commandParameters.getArgs().length ? "All" : commandParameters.getArgs()[argPos];
        final Optional<JavaBoard> board = stat.getBoard(name);
        if (board.isPresent()) {
            return board.get();
        }

        final List<JavaBoard> similarBoards = stat.getSimilarBoard(name, 0.0, 6);
        if (!similarBoards.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarBoards.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "board",
                DiscordBot.getCommandManager()
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[]{game.getName(), stat.getName()},
                similarBoards.stream()
                        .map(JavaBoard::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String name = argPos >= commandParameters.getArgs().length ? "All" : commandParameters.getArgs()[argPos];
        final Optional<JavaBoard> board = stat.getBoard(name);
        if (board.isPresent()) {
            return board.get();
        }

        // TODO: Add error message
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Board")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a valid board. " +
                                "\nTODO: Add help emotes." +
                                "\nHow did you do this?! Just pick all, yearly, weekly, daily or monthly."),
                90
        );
        /*
        final List<JavaBoard> similarBoards = stat.getSimilarBoard(name, 0.0, 6);
        if (!similarBoards.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarBoards.get(0);
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "board", command, new String[]{game.getName(), stat.getName()},
                similarBoards.stream().map(JavaBoard::getName).collect(Collectors.toList()));

         */

        throw new CommandReturnException();
    }

    protected String getPlayer(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        if (NAME_PATTERN.matcher(name).find()) {
            return name;
        }

        throw new CommandReturnException(
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a minecraft name.")
        );
    }

    public JavaGroup getJavaGroup(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaGroup> group = this.getStatsModule().getJavaGroup(name);
        if (group.isPresent()) {
            return group.get();
        }

        final List<JavaGroup> similarGroup = this.getStatsModule().getSimilarJavaGroups(name, 0.6, 3);
        if (!similarGroup.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarGroup.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "group",
                DiscordBot.getCommandManager()
                        .getCommand(JavaGroupsGroupsCommand.class)
                        .orElse(null),
                new String[]{},
                similarGroup.stream()
                        .map(JavaGroup::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    public JavaStat getJavaStat(final JavaGroup group, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        for (final JavaGame game : group.getGames()) {
            if (game.getStat(name).isPresent()) {
                return game.getStat(name).get();
            }
        }

        final List<JavaStat> similarStats = UtilitiesData.getSimilarityList(
                JavaGame.getCleanStat(name),
                group.getStats(),
                JavaStat::getName,
                0.6,
                3
        );
        if (!similarStats.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarStats.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "stat",
                DiscordBot.getCommandManager()
                        .getCommand(JavaGroupsGroupsCommand.class)
                        .orElse(null),
                new String[]{group.getName()},
                similarStats.stream()
                        .map(JavaStat::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }
}
