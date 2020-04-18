package de.timmi6790.statsbotdiscord.modules.mineplexstats;

import de.timmi6790.statsbotdiscord.AbstractModule;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaGamesCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.MpStatsRestApiClient;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGamesModel;
import de.timmi6790.statsbotdiscord.utilities.DataUtilities;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MineplexStatsModule extends AbstractModule {
    @Getter
    private final MpStatsRestApiClient mpStatsRestClient = new MpStatsRestApiClient();

    @Getter
    private final Map<String, JavaGame> javaGames = new ConcurrentHashMap<>();
    private final Map<String, String> javaGamesAlias = new ConcurrentHashMap<>();

    public MineplexStatsModule() {
        super("MineplexStats");
    }

    @Override
    public void onEnable() {
        this.loadJavaGames();

        StatsBot.getCommandManager().registerCommands(
                new JavaGamesCommand()
        );
    }

    @Override
    public void onDisable() {

    }

    public void loadJavaGames() {
        final ResponseModel javaGames = this.mpStatsRestClient.getJavaGames();
        if (!(javaGames instanceof JavaGamesModel)) {
            return;
        }

        final JavaGamesModel javaGamesModel = (JavaGamesModel) javaGames;
        for (final JavaGamesModel.JavaGame game : javaGamesModel.getGames().values()) {
            final String lowerGame = game.getGame().toLowerCase();
            for (final String alias : game.getAliasNames()) {
                this.javaGamesAlias.put(alias.toLowerCase(), lowerGame);
            }

            final Map<String, JavaStat> stats = new HashMap<>();
            final Map<String, String> statAlias = new HashMap<>();
            for (final JavaGamesModel.JavaGameStat gameStat : game.getStats().values()) {
                final String lowerStat = gameStat.getStat().toLowerCase();
                for (final String alias : gameStat.getAliasNames()) {
                    statAlias.put(alias.toLowerCase(), lowerStat);
                }

                final Map<String, String> boards = new HashMap<>();
                final Map<String, String> boardAlias = new HashMap<>();
                for (final JavaGamesModel.JavaGameBoard gameBoard : gameStat.getBoards().values()) {
                    final String lowerBoard = gameBoard.getBoard().toLowerCase();
                    for (final String alias : gameBoard.getAliasNames()) {
                        boardAlias.put(lowerBoard, alias.toLowerCase());
                    }

                    boards.put(lowerBoard, gameBoard.getBoard());
                }

                gameStat.getAliasNames().sort(Comparator.naturalOrder());
                final JavaStat stat = new JavaStat(gameStat.getStat(), gameStat.getAliasNames().toArray(new String[0]), gameStat.getPrettyStat(), gameStat.getDescription(), boards, boardAlias);
                stats.put(lowerStat, stat);
            }

            game.getAliasNames().sort(Comparator.naturalOrder());
            final JavaGame javaGame = new JavaGame(game.getGame(), game.getAliasNames().toArray(new String[0]), game.getCategory(), game.getWikiUrl(), game.getDescription(), stats, statAlias);
            this.javaGames.put(lowerGame, javaGame);
        }
    }

    public Optional<JavaGame> getJavaGame(String name) {
        name = this.javaGamesAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.javaGames.get(name));
    }

    public List<JavaGame> getSimilarGames(final String name, final double similarity, final int limit) {
        final List<JavaGame> similarGames = new ArrayList<>();

        final String[] similarCommandNames = DataUtilities.getSimilarityList(name, this.javaGames.keySet(), similarity).toArray(new String[0]);
        System.out.println(this.javaGames.keySet());

        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGames.add(this.javaGames.get(similarCommandNames[index]));
        }

        return similarGames;
    }
}
