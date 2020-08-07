package de.timmi6790.external_modules.mineplexstats.statsapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.datatypes.MapBuilder;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockGames;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockLeaderboard;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockPlayerStats;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.errors.ErrorModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.*;
import de.timmi6790.external_modules.mineplexstats.statsapi.utilities.JavaGamesModelDeserializer;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MpStatsRestApiClient {
    private static final String BASE_URL = "https://mpstats.timmi6790.de/";//  "http://127.0.0.1:8000/"

    private static final ErrorModel UNKNOWN_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "Unknown Error");
    private static final ErrorModel TIMEOUT_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "API Timeout Exception");

    private final Gson gson;

    private final String authName;
    private final String authPassword;

    public MpStatsRestApiClient(final String authName, final String authPassword) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(JavaGamesModel.class, new JavaGamesModelDeserializer());
        this.gson = gsonBuilder.create();

        Unirest.config().defaultBaseUrl(BASE_URL);
        Unirest.config().connectTimeout(6_000);
        Unirest.config().addDefaultHeader("User-Agent", "MpStatsRestApiClient-Java");

        this.authName = authName;
        this.authPassword = authPassword;

        Unirest.config().setDefaultBasicAuth(this.authName, this.authPassword);
    }

    private ResponseModel makeRequest(final String url, final Map<String, Object> params, final Class<? extends ResponseModel> wantedClazz) {
        try {
            final HttpResponse<JsonNode> response = Unirest.get(url)
                    .queryString(params)
                    .asJson();

            if (!response.isSuccess()) {
                return UNKNOWN_ERROR_RESPONSE_MODEL;
            }

            final JSONObject jsonObject = response.getBody().getObject();
            if (!jsonObject.getBoolean("success")) {
                return this.gson.fromJson(jsonObject.toString(), ErrorModel.class);
            }
            return this.gson.fromJson(jsonObject.toString(), wantedClazz);

        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    public ResponseModel getJavaGames() {
        return this.makeRequest("java/leaderboards/games", new HashMap<>(0), JavaGamesModel.class);
    }

    public ResponseModel getJavaPlayerStats(final String player, final String game, final String board, final long unixTime, final boolean filtering) {
        return this.makeRequest(
                "java/leaderboards/player",
                new MapBuilder<String, Object>(() -> new HashMap<>(5))
                        .put("player", player)
                        .put("game", game)
                        .put("board", board.toLowerCase())
                        .put("date", unixTime)
                        .put("filtering", filtering)
                        .build(),
                JavaPlayerStats.class
        );
    }

    public ResponseModel getJavaLeaderboard(final String game, final String stat, final String board, final int startPos, final int endPos,
                                            final long unixTime, final boolean filtering) {
        return this.makeRequest(
                "java/leaderboards/leaderboard",
                new MapBuilder<String, Object>(() -> new HashMap<>(7))
                        .put("game", game)
                        .put("stat", stat)
                        .put("board", board.toLowerCase())
                        .put("startPosition", startPos)
                        .put("endPosition", endPos)
                        .put("date", unixTime)
                        .put("filtering", filtering)
                        .build(),
                JavaLeaderboard.class
        );
    }

    public ResponseModel getGroups() {
        return this.makeRequest("java/leaderboards/group/groups", new HashMap<>(0), JavaGroupsGroups.class);
    }

    public ResponseModel getPlayerGroup(final String player, final String group, final String stat, final String board, final long unixTime) {
        return this.makeRequest(
                "java/leaderboards/group/player",
                new MapBuilder<String, Object>(() -> new HashMap<>(5))
                        .put("player", player)
                        .put("group", group)
                        .put("stat", stat)
                        .put("board", board.toLowerCase())
                        .put("date", unixTime)
                        .build(),
                JavaGroupsPlayer.class
        );
    }

    public ResponseModel getPlayerStatsRatio(final String player, final String stat, final String board, final long unixTime) {
        return this.makeRequest(
                "java/leaderboards/ratio/player",
                new MapBuilder<String, Object>(() -> new HashMap<>(4))
                        .put("player", player)
                        .put("stat", stat)
                        .put("board", board.toLowerCase())
                        .put("date", unixTime)
                        .build(),
                JavaRatioPlayer.class
        );
    }

    // Bedrock
    public ResponseModel getBedrockGames() {
        return this.makeRequest("bedrock/leaderboards/games", new HashMap<>(0), BedrockGames.class);
    }

    public ResponseModel getBedrockLeaderboard(final String game, final int startPos, final int endPos, final long unixTime) {
        return this.makeRequest(
                "bedrock/leaderboards/leaderboard",
                new MapBuilder<String, Object>(() -> new HashMap<>(4))
                        .put("game", game)
                        .put("startPosition", startPos)
                        .put("endPosition", endPos)
                        .put("date", unixTime)
                        .build(),
                BedrockLeaderboard.class
        );
    }

    public ResponseModel getBedrockPlayerStats(final String player) {
        return this.makeRequest(
                "bedrock/leaderboards/player",
                new MapBuilder<String, Object>(() -> new HashMap<>(1))
                        .put("name", player)
                        .build(),
                BedrockPlayerStats.class
        );
    }

    // Internal
    public void addJavaPlayerFilter(final UUID uuid, final String game, final String stat, final String board) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        Unirest.post("java/leaderboards/filter")
                .basicAuth(this.authName, this.authPassword)
                .queryString("game", game)
                .queryString("stat", stat)
                .queryString("board", board.toLowerCase())
                .queryString("uuid", uuid.toString())
                .asEmpty();
    }

    public void addBedrockPlayerFilter(final String player, final String game) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        Unirest.post("bedrock/leaderboards/filter")
                .basicAuth(this.authName, this.authPassword)
                .queryString("game", game)
                .queryString("name", player)
                .asEmpty();
    }

    public void addJavaBoardAlias(final String board, final String alias) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        Unirest.post("java/leaderboards/alias/board")
                .basicAuth(this.authName, this.authPassword)
                .queryString("board", board.toLowerCase())
                .queryString("alias", alias)
                .asEmpty();
    }

    public void addJavaGameAlias(final String game, final String alias) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        Unirest.post("java/leaderboards/alias/game")
                .basicAuth(this.authName, this.authPassword)
                .queryString("game", game)
                .queryString("alias", alias)
                .asEmpty();
    }

    public void addJavaStatAlias(final String game, final String stat, final String alias) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        Unirest.post("java/leaderboards/alias/stat")
                .basicAuth(this.authName, this.authPassword)
                .queryString("game", game)
                .queryString("stat", stat)
                .queryString("alias", alias)
                .asEmpty();
    }
}
