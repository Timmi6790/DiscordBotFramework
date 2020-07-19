package de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock;

import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BedrockLeaderboard extends ResponseModel {
    private final Info info;
    private final List<Leaderboard> leaderboard;

    @Data
    @AllArgsConstructor
    public static class Info {
        private final String game;
        private final boolean filter;
        private final int startPosition;
        private final int endPosition;
        private final int totalLength;
        private final long unix;
    }

    @Data
    @AllArgsConstructor
    public static class Leaderboard {
        private final String name;
        private final int position;
        private final long score;
    }
}
