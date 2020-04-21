package de.timmi6790.statsbotdiscord.modules.mineplexstats;

import de.timmi6790.statsbotdiscord.AbstractModule;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaGamesCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaGroupsGroupsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaLeaderboardCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaPlayerGroupCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.MpStatsRestApiClient;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGamesModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGroup;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGroupsGroups;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MineplexStatsModule extends AbstractModule {
    @Getter
    private final MpStatsRestApiClient mpStatsRestClient = new MpStatsRestApiClient();

    @Getter
    private final Map<String, JavaGame> javaGames = new ConcurrentHashMap<>();
    private final Map<String, String> javaGamesAlias = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, JavaGroup> javaGroups = new ConcurrentHashMap<>();
    private final Map<String, String> javaGroupsAlias = new ConcurrentHashMap<>();

    public MineplexStatsModule() {
        super("MineplexStats");
    }

    @Override
    public void onEnable() {
        this.loadJavaGames();
        this.loadJavaGroups();

        StatsBot.getCommandManager().registerCommands(
                new JavaGamesCommand(),
                new JavaPlayerGroupCommand(),
                new JavaGroupsGroupsCommand(),
                new JavaLeaderboardCommand()
        );
    }

    @Override
    public void onDisable() {

    }

    public void loadJavaGames() {
        final ResponseModel responseModel = this.mpStatsRestClient.getJavaGames();
        if (!(responseModel instanceof JavaGamesModel)) {
            return;
        }

        for (final JavaGame javaGame : ((JavaGamesModel) responseModel).getGames().values()) {
            this.javaGames.put(javaGame.getName().toLowerCase(), javaGame);

            for (final String alias : javaGame.getAliasNames()) {
                this.javaGamesAlias.put(alias.toLowerCase(), javaGame.getName().toLowerCase());
            }
        }
    }

    public void loadJavaGroups() {
        final ResponseModel responseModel = this.mpStatsRestClient.getGroups();
        if (!(responseModel instanceof JavaGroupsGroups)) {
            return;
        }

        for (final JavaGroup javaGroup : ((JavaGroupsGroups) responseModel).getGroups().values()) {
            Arrays.sort(javaGroup.getAliasNames());
            javaGroup.getGameNames().sort(Comparator.naturalOrder());

            this.javaGroups.put(javaGroup.getName().toLowerCase(), javaGroup);

            for (final String alias : javaGroup.getAliasNames()) {
                this.javaGroupsAlias.put(alias.toLowerCase(), javaGroup.getName().toLowerCase());
            }
        }
    }

    public Optional<JavaGame> getJavaGame(String name) {
        name = this.javaGamesAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.javaGames.get(name));
    }

    public List<JavaGame> getSimilarGames(final String name, final double similarity, final int limit) {
        final List<JavaGame> similarGames = new ArrayList<>();

        final String[] similarCommandNames = UtilitiesData.getSimilarityList(name, this.javaGames.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGames.add(this.javaGames.get(similarCommandNames[index]));
        }

        return similarGames;
    }

    public Optional<JavaGroup> getJavaGroup(String name) {
        name = this.javaGroupsAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.javaGroups.get(name));
    }

    public List<JavaGroup> getSimilarGroups(final String name, final double similarity, final int limit) {
        final List<JavaGroup> similarGroups = new ArrayList<>();

        final String[] similarCommandNames = UtilitiesData.getSimilarityList(name, this.javaGroups.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGroups.add(this.javaGroups.get(similarCommandNames[index]));
        }

        return similarGroups;
    }
}
