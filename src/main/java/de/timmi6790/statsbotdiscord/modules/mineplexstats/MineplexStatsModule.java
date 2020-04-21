package de.timmi6790.statsbotdiscord.modules.mineplexstats;

import de.timmi6790.statsbotdiscord.AbstractModule;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaGamesCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaPlayerGroupCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.MpStatsRestApiClient;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGamesModel;
import de.timmi6790.statsbotdiscord.utilities.DataUtilities;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                new JavaGamesCommand(),
                new JavaPlayerGroupCommand()
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

        final JavaGamesModel gamesModel = (JavaGamesModel) javaGames;
        for (final JavaGame javaGame : gamesModel.getGames().values()) {
            this.javaGames.put(javaGame.getName().toLowerCase(), javaGame);

            for (final String alias : javaGame.getAliasNames()) {
                this.javaGamesAlias.put(alias.toLowerCase(), javaGame.getName().toLowerCase());
            }
        }
    }

    public Optional<JavaGame> getJavaGame(String name) {
        name = this.javaGamesAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.javaGames.get(name));
    }

    public List<JavaGame> getSimilarGames(final String name, final double similarity, final int limit) {
        final List<JavaGame> similarGames = new ArrayList<>();

        final String[] similarCommandNames = DataUtilities.getSimilarityList(name, this.javaGames.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGames.add(this.javaGames.get(similarCommandNames[index]));
        }

        return similarGames;
    }
}
