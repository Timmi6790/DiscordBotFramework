package de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock;

import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class BedrockPlayerStats extends ResponseModel {
    private final Info info;
    private final Map<String, Stats> stats;


    @Data
    @AllArgsConstructor
    public static class Info {
        private final String name;
        private final boolean filter;
    }

    @Data
    @AllArgsConstructor
    public static class Stats {
        private final String game;
        private final int position;
        private final int score;
        private final int unix;
    }
}
