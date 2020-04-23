package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class JavaLeaderboard extends ResponseModel {
    private final Info info;
    private final List<Leaderboard> leaderboard;


    @Data
    @AllArgsConstructor
    public static class Info {
        private final String game;
        private final String stat;
        private final String prettyStat;
        private final String board;
        private final boolean filter;
        private final int startPosition;
        private final int endPosition;
        private final int totalLength;
        private final long unix;
    }

    @Data
    @AllArgsConstructor
    public static class Leaderboard {
        private final UUID uuid;
        private final String name;
        private final int position;
        private final long score;
    }
}
