package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.AbstractStatsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGroup;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
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

public abstract class AbstractJavaStatsCommand extends AbstractStatsCommand {
    private final static Pattern NAME_PATTERN = Pattern.compile("^\\w{1,16}$");
    private final static List<String> STATS_TIME = new ArrayList<>(Arrays.asList("Ingame Time", "Hub Time", "Time Playing"));

    private final static Cache<UUID, BufferedImage> SKIN_CACHE = Caffeine.newBuilder()
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

        final List<String> similarNames = new ArrayList<>();
        for (final JavaGame similarGame : similarGames) {
            similarNames.add(similarGame.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "game", command, new String[0], similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    protected JavaStat getStat(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaStat> stat = game.getStat(name);
        if (stat.isPresent()) {
            return stat.get();
        }

        final JavaStat[] similarStats = game.getSimilarStats(JavaGame.getCleanStat(name), 0.6, 3).toArray(new JavaStat[0]);
        if (similarStats.length != 0 && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarStats[0];
        }

        final List<String> similarNames = new ArrayList<>();
        for (final JavaStat similarStat : similarStats) {
            similarNames.add(similarStat.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "stat", command, new String[]{game.getName()}, similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = argPos >= commandParameters.getArgs().length ? "All" : commandParameters.getArgs()[argPos];

        for (final JavaStat stat : game.getStats().values()) {
            if (stat.getBoard(name).isPresent()) {
                return stat.getBoard(name).get();
            }
        }

        final Set<String> boards = new HashSet<>();
        JavaStat exampleStat = null;
        for (final JavaStat stat : game.getStats().values()) {
            boards.addAll(stat.getBoardNames());
            exampleStat = stat;
        }

        final List<String> similarBoards = UtilitiesData.getSimilarityList(name, boards, 0.0);
        if (!similarBoards.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            final String newBoard = similarBoards.get(0);
            for (final JavaStat stat : game.getStats().values()) {
                for (final JavaBoard board : stat.getBoards().values()) {
                    if (board.getName().equalsIgnoreCase(newBoard)) {
                        return board;
                    }
                }
            }
        }

        final String[] similarNames = new String[Math.min(similarBoards.size(), 6)];
        for (int index = 0; similarNames.length > index; index++) {
            similarNames[index] = similarBoards.get(index);
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "board", command, new String[]{game.getName(), exampleStat != null ? exampleStat.getName() : ""}, similarNames);

        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String name = argPos >= commandParameters.getArgs().length ? "All" : commandParameters.getArgs()[argPos];

        final Optional<JavaBoard> board = stat.getBoard(name);
        if (board.isPresent()) {
            return board.get();
        }

        final JavaBoard[] similarBoards = stat.getSimilarBoard(name, 0.0, 6).toArray(new JavaBoard[0]);
        if (similarBoards.length != 0 && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarBoards[0];
        }

        final List<String> similarNames = new ArrayList<>();
        for (final JavaBoard similar : similarBoards) {
            similarNames.add(similar.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "board", command, new String[]{game.getName(), stat.getName()}, similarNames.toArray(new String[0]));

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

        final JavaGroup[] similarGroup = this.getStatsModule().getSimilarJavaGroups(name, 0.6, 3).toArray(new JavaGroup[0]);
        if (similarGroup.length != 0 && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarGroup[0];
        }

        final List<String> similarNames = new ArrayList<>();
        for (final JavaGroup similar : similarGroup) {
            similarNames.add(similar.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "group", command, new String[]{}, similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    public JavaStat getJavaStat(final JavaGroup group, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        for (final JavaGame game : group.getGames()) {
            if (game.getStat(name).isPresent()) {
                return game.getStat(name).get();
            }
        }

        final Set<String> statNames = new HashSet<>();
        for (final JavaStat stat : group.getStats()) {
            statNames.add(stat.getName());
        }

        final List<String> similarStats = UtilitiesData.getSimilarityList(JavaGame.getCleanStat(name), statNames, 0.6);
        if (!similarStats.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            final String newStat = similarStats.get(0);
            for (final JavaStat stat : group.getStats()) {
                if (stat.getName().equalsIgnoreCase(newStat)) {
                    return stat;
                }
            }
        }

        final String[] similarNames = new String[Math.min(similarStats.size(), 3)];
        for (int index = 0; similarNames.length > index; index++) {
            similarNames[index] = similarStats.get(index);
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGroupsGroupsCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "stat", command, new String[]{group.getName()}, similarNames);

        throw new CommandReturnException();
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
                    .connectTimeout(10_000)
                    .asBytes();

            if (!response.isSuccess()) {
                completableFuture.complete(null);
                return;
            }

            try {
                final InputStream in = new ByteArrayInputStream(response.getBody());
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
}
