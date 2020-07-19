package de.timmi6790.external_modules.mineplexstats.statsapi.models.java;

import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class JavaRatioPlayer extends ResponseModel {
    private final JavaRatioPlayer.Info info;
    private final Map<String, JavaRatioPlayer.Stat> stats;


    @Data
    @AllArgsConstructor
    public static class Info {
        private final UUID uuid;
        private final String name;
        private final long totalNumber;
        private final String stat;
        private final String board;
        private final boolean filter;
    }

    @Data
    @AllArgsConstructor
    public static class Stat {
        private final String game;
        private final int score;
        private final int unix;
    }
}
