package de.timmi6790.external_modules.mineplexstats.statsapi.utilities;

import com.google.gson.*;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGamesModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaStat;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JavaGamesModelDeserializer implements JsonDeserializer<JavaGamesModel> {
    @Override
    public JavaGamesModel deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final Gson gson = new Gson();

        final Map<String, JavaGame> parsedGames = new HashMap<>();
        final JsonObject gamesObject = json.getAsJsonObject().getAsJsonObject("games");
        for (final String gameName : gamesObject.keySet()) {
            final JsonObject game = gamesObject.getAsJsonObject(gameName);

            final Map<String, JavaStat> stats = new HashMap<>();
            final JsonObject statsObject = game.getAsJsonObject("stats");
            for (final String statName : statsObject.keySet()) {
                final JsonObject stat = statsObject.getAsJsonObject(statName);

                final Map<String, JavaBoard> boards = new HashMap<>();
                final JsonObject boardsObject = stat.getAsJsonObject("boards");
                for (final String boardName : boardsObject.keySet()) {
                    final JsonObject board = boardsObject.getAsJsonObject(boardName);
                    boards.put(
                            boardName.toLowerCase(),
                            new JavaBoard(
                                    board.get("board").getAsString(),
                                    gson.fromJson(board.getAsJsonArray("aliasNames").toString(), String[].class)
                            )
                    );
                }

                stats.put(
                        JavaGame.getCleanStat(statName).toLowerCase(),
                        new JavaStat(
                                stat.get("stat").getAsString(),
                                gson.fromJson(stat.getAsJsonArray("aliasNames").toString(), String[].class),
                                stat.get("achievement").getAsBoolean(),
                                stat.get("description").getAsString(),
                                boards
                        )
                );
            }

            parsedGames.put(
                    gameName.toLowerCase(),
                    new JavaGame(
                            game.get("game").getAsString(),
                            gson.fromJson(game.getAsJsonArray("aliasNames").toString(), String[].class),
                            game.get("category").getAsString(),
                            game.get("wikiUrl").getAsString(),
                            game.get("description").getAsString(),
                            stats
                    )
            );
        }

        return new JavaGamesModel(parsedGames);
    }
}
