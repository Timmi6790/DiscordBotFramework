package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class PlayerStats extends ResponseModel {
    private final PlayerStatsInfo info;
    private final Map<String, PlayerStatsStats> stats;


    @Data
    @AllArgsConstructor
    public static class PlayerStatsInfo {
        private final UUID uuid;
        private final String name;
        private final String game;
        private final String board;
        private final boolean filter;
    }

    @Data
    @AllArgsConstructor
    public static class PlayerStatsStats {
        private final String stat;
        private final String prettyStat;
        private final int position;
        private final int score;
        private final int unix;
    }
}
