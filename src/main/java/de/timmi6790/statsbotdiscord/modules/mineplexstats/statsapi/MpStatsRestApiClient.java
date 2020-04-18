package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi;

import com.google.gson.Gson;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.errors.ErrorModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGamesModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.PlayerStats;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class MpStatsRestApiClient {
    private static final String USER_AGENT = "MpStatsRestApiClient-Java";
    private static final String BASE_URL = "http://127.0.0.1:8001/"; // "https://mpstats2.timmi6790.de/";

    public static void main(final String[] args) {
        final MpStatsRestApiClient restApiClient = new MpStatsRestApiClient();

        System.out.println(restApiClient.getPlayerStats("Timmi6790", "Globald", "all"));
    }

    public void getLeaderboardSaves(final String game, final String board) {
        final String url = MpStatsRestApiClient.BASE_URL + "java/leaderboards/saves?" + "game=" + game + "&board=" + board;
    }

    public ResponseModel getJavaGames() {
        final HttpResponse<JsonNode> response = Unirest.get(MpStatsRestApiClient.BASE_URL + "java/leaderboards/games")
                .header("User-Agent", USER_AGENT)
                .connectTimeout(6_000)
                .asJson();

        if (!response.isSuccess()) {
            System.out.println("Error: " + response.getBody() + " " + response.getStatus());
            return new ErrorModel(0, "Unknown Error");
        }

        final Gson gson = new Gson();
        final JSONObject jsonObject = response.getBody().getObject();
        if (!jsonObject.getBoolean("success")) {
            return gson.fromJson(jsonObject.toString(), ErrorModel.class);
        }

        return gson.fromJson(jsonObject.toString(), JavaGamesModel.class);
    }

    public ResponseModel getPlayerStats(final String player, final String game, final String board) {
        final HttpResponse<JsonNode> response = Unirest.get(MpStatsRestApiClient.BASE_URL + "java/leaderboards/player")
                .queryString("player", player)
                .queryString("game", game)
                .queryString("board", board)
                .header("User-Agent", USER_AGENT)
                .connectTimeout(6_000)
                .asJson();

        if (!response.isSuccess()) {
            System.out.println("Error: " + response.getBody() + " " + response.getStatus());
            return new ErrorModel(0, "Unknown Error");
        }

        final Gson gson = new Gson();
        final JSONObject jsonObject = response.getBody().getObject();
        if (!jsonObject.getBoolean("success")) {
            return gson.fromJson(jsonObject.toString(), ErrorModel.class);
        }

        return gson.fromJson(jsonObject.toString(), PlayerStats.class);
    }

    public ResponseModel getLeaderboard(final String game, final String stat, final String board) {
        return null;
    }
}
