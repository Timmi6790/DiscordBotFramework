package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi;

import com.google.gson.Gson;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockGames;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockLeaderboard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockPlayerStats;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.errors.ErrorModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.*;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MpStatsRestApiClient {
    private static final String BASE_URL = "https://mpstats.timmi6790.de/";

    private final static ErrorModel UNKNOWN_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "Unknown Error");
    private final static ErrorModel TIMEOUT_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "API Timeout Exception");

    private final Gson gson = new Gson();

    private final String authName;
    private final String authPassword;

    public MpStatsRestApiClient() {
        Unirest.config().defaultBaseUrl(BASE_URL);
        Unirest.config().connectTimeout(6_000);
        Unirest.config().addDefaultHeader("User-Agent", "MpStatsRestApiClient-Java");

        this.authName = StatsBot.getConfig().getString("mpStatsApi.name");
        this.authPassword = StatsBot.getConfig().getString("mpStatsApi.password");
    }

    public ResponseModel parseHttpResponse(final HttpResponse<JsonNode> response, final Class<? extends ResponseModel> clazz) {
        if (!response.isSuccess()) {
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }

        final JSONObject jsonObject = response.getBody().getObject();
        if (!jsonObject.getBoolean("success")) {
            return this.gson.fromJson(jsonObject.toString(), ErrorModel.class);
        }

        return this.gson.fromJson(jsonObject.toString(), clazz);
    }

    public ResponseModel getJavaGames() {
        final HttpResponse<JsonNode> response;
        try {
            response = Unirest.get("java/leaderboards/games")
                    .asJson();
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }

        if (!response.isSuccess()) {
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }

        final JSONObject jsonObject = response.getBody().getObject();
        if (!jsonObject.getBoolean("success")) {
            return this.gson.fromJson(jsonObject.toString(), ErrorModel.class);
        }

        final Map<String, JavaGame> parsedGames = new HashMap<>();

        final JSONObject gamesObject = jsonObject.getJSONObject("games");
        for (final Iterator<String> it = gamesObject.keys(); it.hasNext(); ) {
            final String gameName = it.next();
            final JSONObject game = gamesObject.getJSONObject(gameName);

            final Map<String, JavaStat> stats = new HashMap<>();

            final JSONObject statsObject = game.getJSONObject("stats");
            for (final Iterator<String> iter = statsObject.keys(); iter.hasNext(); ) {
                final String statName = iter.next();
                final JSONObject stat = statsObject.getJSONObject(statName);

                final Map<String, JavaBoard> boards = new HashMap<>();

                final JSONObject boardsObject = stat.getJSONObject("boards");
                for (final Iterator<String> iterator = boardsObject.keys(); iterator.hasNext(); ) {
                    final String boardName = iterator.next();
                    final JSONObject board = boardsObject.getJSONObject(boardName);

                    boards.put(
                            boardName.toLowerCase(),
                            new JavaBoard(
                                    board.getString("board"),
                                    this.gson.fromJson(board.getJSONArray("aliasNames").toString(), String[].class)
                            )
                    );

                }

                stats.put(
                        statName.toLowerCase(),
                        new JavaStat(
                                stat.getString("stat"),
                                this.gson.fromJson(stat.getJSONArray("aliasNames").toString(), String[].class),
                                stat.getString("prettyStat"),
                                stat.getString("description"),
                                boards
                        )
                );
            }

            parsedGames.put(
                    gameName.toLowerCase(),
                    new JavaGame(
                            game.getString("game"),
                            this.gson.fromJson(game.getJSONArray("aliasNames").toString(), String[].class),
                            game.getString("category"),
                            game.getString("wikiUrl"),
                            game.getString("description"),
                            stats
                    )
            );
        }

        return new JavaGamesModel(parsedGames);
    }

    public ResponseModel getJavaPlayerStats(final String player, final String game, final String board, final long unixTime) {
        try {
            final HttpResponse<JsonNode> response = Unirest.get("java/leaderboards/player")
                    .queryString("player", player)
                    .queryString("game", game)
                    .queryString("board", board.toLowerCase())
                    .queryString("date", unixTime)
                    .asJson();

            return this.parseHttpResponse(response, JavaPlayerStats.class);
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    public ResponseModel getJavaLeaderboard(final String game, final String stat, final String board, final int startPos, final int endPos, final long unixTime) {
        try {
            final HttpResponse<JsonNode> response = Unirest.get("java/leaderboards/leaderboard")
                    .queryString("game", game)
                    .queryString("stat", stat)
                    .queryString("board", board.toLowerCase())
                    .queryString("startPosition", startPos)
                    .queryString("endPosition", endPos)
                    .queryString("date", unixTime)
                    .asJson();

            return this.parseHttpResponse(response, JavaLeaderboard.class);
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    public ResponseModel getGroups() {
        try {
            final HttpResponse<JsonNode> response = Unirest.get("java/leaderboards/group/groups")
                    .asJson();

            return this.parseHttpResponse(response, JavaGroupsGroups.class);
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    public ResponseModel getPlayerGroup(final String player, final String group, final String stat, final String board, final long unixTime) {
        try {
            final HttpResponse<JsonNode> response = Unirest.get("java/leaderboards/group/player")
                    .queryString("player", player)
                    .queryString("group", group)
                    .queryString("stat", stat)
                    .queryString("board", board.toLowerCase())
                    .queryString("date", unixTime)
                    .asJson();

            return this.parseHttpResponse(response, JavaGroupsPlayer.class);
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    // Bedrock
    public ResponseModel getBedrockGames() {
        try {
            final HttpResponse<JsonNode> response = Unirest.get("bedrock/leaderboards/games")
                    .asJson();

            return this.parseHttpResponse(response, BedrockGames.class);
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    public ResponseModel getBedrockLeaderboard(final String game, final int startPos, final int endPos, final long unixTime) {
        try {
            final HttpResponse<JsonNode> response = Unirest.get("bedrock/leaderboards/leaderboard")
                    .queryString("game", game)
                    .queryString("startPosition", startPos)
                    .queryString("endPosition", endPos)
                    .queryString("date", unixTime)
                    .asJson();

            return this.parseHttpResponse(response, BedrockLeaderboard.class);
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    public ResponseModel getBedrockPlayerStats(final String player) {
        try {
            final HttpResponse<JsonNode> response = Unirest.get("bedrock/leaderboards/player")
                    .queryString("name", player)
                    .asJson();

            return this.parseHttpResponse(response, BedrockPlayerStats.class);
        } catch (final UnirestException e) {
            e.printStackTrace();
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            e.printStackTrace();
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

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
}
