package de.timmi6790.statsbotdiscord.modules.mineplexstats;

import de.timmi6790.statsbotdiscord.AbstractModule;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock.BedrockGamesCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock.BedrockLeaderboardCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock.BedrockPlayerCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.debug.ReloadDataCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.*;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.MpStatsRestApiClient;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockGames;
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

    @Getter
    private final Map<String, String> bedrockGames = new ConcurrentHashMap<>();

    public MineplexStatsModule() {
        super("MineplexStats");
    }

    @Override
    public void onEnable() {
        // Maybe I should handle the api downtime better
        try {
            this.loadJavaGames();
            this.loadJavaGroups();

            this.loadBedrockGames();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        StatsBot.getCommandManager().registerCommands(
                new JavaGamesCommand(),
                new JavaPlayerStatsCommand(),
                new JavaPlayerGroupCommand(),
                new JavaGroupsGroupsCommand(),
                new JavaLeaderboardCommand(),

                new BedrockGamesCommand(),
                new BedrockPlayerCommand(),
                new BedrockLeaderboardCommand(),

                new ReloadDataCommand()
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

        this.javaGames.clear();
        this.javaGamesAlias.clear();
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

        this.javaGroups.clear();
        this.javaGroupsAlias.clear();
        for (final JavaGroup javaGroup : ((JavaGroupsGroups) responseModel).getGroups().values()) {
            Arrays.sort(javaGroup.getAliasNames());
            javaGroup.getGameNames().sort(Comparator.naturalOrder());

            this.javaGroups.put(javaGroup.getName().toLowerCase(), javaGroup);

            for (final String alias : javaGroup.getAliasNames()) {
                this.javaGroupsAlias.put(alias.toLowerCase(), javaGroup.getName().toLowerCase());
            }
        }
    }

    public void loadBedrockGames() {
        final ResponseModel responseModel = this.mpStatsRestClient.getBedrockGames();
        if (!(responseModel instanceof BedrockGames)) {
            return;
        }

        this.bedrockGames.clear();
        for (final String game : ((BedrockGames) responseModel).getGames()) {
            this.bedrockGames.put(game.toLowerCase(), game);
        }
    }

    public Optional<JavaGame> getJavaGame(String name) {
        name = this.javaGamesAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.javaGames.get(name));
    }

    public List<JavaGame> getSimilarJavaGames(final String name, final double similarity, final int limit) {
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

    public List<JavaGroup> getSimilarJavaGroups(final String name, final double similarity, final int limit) {
        final List<JavaGroup> similarGroups = new ArrayList<>();

        final String[] similarCommandNames = UtilitiesData.getSimilarityList(name, this.javaGroups.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGroups.add(this.javaGroups.get(similarCommandNames[index]));
        }

        return similarGroups;
    }

    public Optional<String> getBedrockGame(final String name) {
        return Optional.ofNullable(this.bedrockGames.get(name.toLowerCase()));
    }

    public List<String> getSimilarBedrockGames(final String name, final double similarity, final int limit) {
        final List<String> similarGames = new ArrayList<>();

        final String[] similarCommandNames = UtilitiesData.getSimilarityList(name, this.bedrockGames.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGames.add(this.bedrockGames.get(similarCommandNames[index]));
        }

        return similarGames;
    }

}
