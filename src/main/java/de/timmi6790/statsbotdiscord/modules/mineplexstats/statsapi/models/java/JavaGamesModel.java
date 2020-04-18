package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class JavaGamesModel extends ResponseModel {
    private final Map<String, JavaGame> games;

    @Data
    @AllArgsConstructor
    public static class JavaGame {
        private final String game;
        private final List<String> aliasNames;
        private final String category;
        private final String wikiUrl;
        private final String description;
        private final Map<String, JavaGameStat> stats;
    }

    @Data
    @AllArgsConstructor
    public static class JavaGameStat {
        private final String stat;
        private final String prettyStat;
        private final List<String> aliasNames;
        private final String description;
        private final Map<String, JavaGameBoard> boards;
    }

    @Data
    @AllArgsConstructor
    public static class JavaGameBoard {
        private final String board;
        private final List<String> aliasNames;
    }
}
