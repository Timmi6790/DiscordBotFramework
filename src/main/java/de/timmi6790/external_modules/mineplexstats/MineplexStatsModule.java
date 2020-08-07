package de.timmi6790.external_modules.mineplexstats;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.utilities.UtilitiesData;
import de.timmi6790.external_modules.mineplexstats.commands.bedrock.BedrockGamesCommand;
import de.timmi6790.external_modules.mineplexstats.commands.bedrock.BedrockLeaderboardCommand;
import de.timmi6790.external_modules.mineplexstats.commands.bedrock.BedrockPlayerCommand;
import de.timmi6790.external_modules.mineplexstats.commands.bedrock.management.BedrockFilterLeaderboardCommand;
import de.timmi6790.external_modules.mineplexstats.commands.bedrock.management.BedrockPlayerFilterCommand;
import de.timmi6790.external_modules.mineplexstats.commands.debug.ReloadDataCommand;
import de.timmi6790.external_modules.mineplexstats.commands.java.*;
import de.timmi6790.external_modules.mineplexstats.commands.java.management.*;
import de.timmi6790.external_modules.mineplexstats.commands.java.unfiltered.JavaUnfilteredLeaderboardCommand;
import de.timmi6790.external_modules.mineplexstats.commands.java.unfiltered.JavaUnfilteredPlayerStatsCommand;
import de.timmi6790.external_modules.mineplexstats.statsapi.MpStatsRestApiClient;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockGames;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGamesModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGroup;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGroupsGroups;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MineplexStatsModule extends AbstractModule {
    @Getter
    private MpStatsRestApiClient mpStatsRestClient;

    @Getter
    private final Map<String, JavaGame> javaGames = new ConcurrentHashMap<>();
    private final Map<String, String> javaGamesAlias = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, JavaGroup> javaGroups = new ConcurrentHashMap<>();
    private final Map<String, String> javaGroupsAlias = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, BedrockGame> bedrockGames = new ConcurrentHashMap<>();

    public MineplexStatsModule() {
        super("MineplexStats");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                CommandModule.class
        );
    }

    @Override
    public void onEnable() {
        final Config statsConfig = DiscordBot.getModuleManager()
                .getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());
        this.mpStatsRestClient = new MpStatsRestApiClient(statsConfig.getApiName(), statsConfig.getApiPassword());

        // I should maybe handle the api downtime better
        this.loadJavaGames();
        this.loadJavaGroups();

        this.loadBedrockGames();

        DiscordBot.getModuleManager()
                .getModuleOrThrow(CommandModule.class)
                .registerCommands(
                        this,
                        new JavaGamesCommand(),
                        new JavaPlayerStatsCommand(),
                        new JavaPlayerGroupCommand(),
                        new JavaGroupsGroupsCommand(),
                        new JavaLeaderboardCommand(),
                        new JavaPlayerStatsRatioCommand(),

                        new BedrockGamesCommand(),
                        new BedrockPlayerCommand(),
                        new BedrockLeaderboardCommand(),

                        new ReloadDataCommand(),

                        new JavaUUUIDLeaderboardCommand(),
                        new JavaPlayerFilterCommand(),
                        new JavaGameAliasCommand(),
                        new JavaBoardAliasCommand(),
                        new JavaStatAliasCommand(),

                        new BedrockPlayerFilterCommand(),
                        new BedrockFilterLeaderboardCommand(),

                        new JavaUnfilteredLeaderboardCommand(),
                        new JavaUnfilteredPlayerStatsCommand()
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
        ((JavaGamesModel) responseModel).getGames().values().forEach(javaGame -> {
            this.javaGames.put(javaGame.getName().toLowerCase(), javaGame);
            Arrays.stream(javaGame.getAliasNames()).forEach(alias -> this.javaGamesAlias.put(alias.toLowerCase(), javaGame.getName().toLowerCase()));
        });
    }

    public void loadJavaGroups() {
        final ResponseModel responseModel = this.mpStatsRestClient.getGroups();
        if (!(responseModel instanceof JavaGroupsGroups)) {
            return;
        }

        this.javaGroups.clear();
        this.javaGroupsAlias.clear();
        ((JavaGroupsGroups) responseModel).getGroups().values().forEach(javaGroup -> {
            Arrays.sort(javaGroup.getAliasNames());
            javaGroup.getGameNames().sort(Comparator.naturalOrder());

            this.javaGroups.put(javaGroup.getName().toLowerCase(), javaGroup);
            Arrays.stream(javaGroup.getAliasNames()).forEach(alias -> this.javaGroupsAlias.put(alias.toLowerCase(), javaGroup.getName().toLowerCase()));
        });
    }

    public void loadBedrockGames() {
        final ResponseModel responseModel = this.mpStatsRestClient.getBedrockGames();
        if (!(responseModel instanceof BedrockGames)) {
            return;
        }

        this.bedrockGames.clear();
        ((BedrockGames) responseModel).getGames().forEach(game -> this.bedrockGames.put(game.getName().toLowerCase(), game));
    }

    // Get Data
    public Optional<JavaGame> getJavaGame(String name) {
        name = this.javaGamesAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.javaGames.get(name));
    }

    public List<JavaGame> getSimilarJavaGames(final String name, final double similarity, final int limit) {
        return UtilitiesData.getSimilarityList(name, this.javaGames.keySet(), similarity, limit)
                .stream()
                .map(this.javaGames::get)
                .collect(Collectors.toList());
    }

    public Optional<JavaGroup> getJavaGroup(String name) {
        name = this.javaGroupsAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.javaGroups.get(name));
    }

    public List<JavaGroup> getSimilarJavaGroups(final String name, final double similarity, final int limit) {
        return UtilitiesData.getSimilarityList(name, this.javaGroups.keySet(), similarity, limit)
                .stream()
                .map(this.javaGroups::get)
                .collect(Collectors.toList());
    }

    public Optional<BedrockGame> getBedrockGame(final String name) {
        return Optional.ofNullable(this.bedrockGames.get(name.toLowerCase()));
    }

    public List<BedrockGame> getSimilarBedrockGames(final String name, final double similarity, final int limit) {
        return UtilitiesData.getSimilarityList(
                name,
                this.bedrockGames.keySet(),
                similarity,
                limit)
                .stream()
                .map(this.bedrockGames::get)
                .collect(Collectors.toList());
    }

}
