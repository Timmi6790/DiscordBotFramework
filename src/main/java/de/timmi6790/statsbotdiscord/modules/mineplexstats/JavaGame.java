package de.timmi6790.statsbotdiscord.modules.mineplexstats;

import de.timmi6790.statsbotdiscord.utilities.DataUtilities;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@AllArgsConstructor
@Data
public class JavaGame {
    private static final List<String> STATS_ORDER = new ArrayList<>(Arrays.asList("Wins", "TimeInGame", "TimeInHub", "GamesPlayed", "DailyVote", "ClansDailyReward", "DailyReward",
            "CrownsEarned", "BestWinStreak", "SecondPlace", "ThirdPlace", "Losses", "Kills", "FinalKills", "Assists", "Deaths"));

    static {
        Collections.reverse(STATS_ORDER);
    }

    private final String name;
    private final String[] aliasNames;
    private final String category;
    private final String wikiUrl;
    private final String description;

    private final Map<String, JavaStat> stats;
    private final Map<String, String> statAlias;
    private final List<String> sortedStatsNames = new ArrayList<>();

    public Optional<JavaStat> getStat(String name) {
        name = this.statAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.stats.get(name));
    }

    public List<String> getStatNames() {
        if (this.sortedStatsNames.isEmpty()) {
            final List<JavaStat> stats = new ArrayList<>(this.stats.values());
            stats.sort((first, second) -> {
                if (first.equals(second)) {
                    return 0;
                }

                final int firstIndex = STATS_ORDER.indexOf(first.getName());
                final int secondIndex = STATS_ORDER.indexOf(second.getName());
                if (Math.max(secondIndex, firstIndex) != -1) {
                    return Integer.compare(secondIndex, firstIndex);
                }

                final boolean firstAchievement = first.getPrettyStat().startsWith("Achievement");
                final boolean secondAchievement = second.getPrettyStat().startsWith("Achievement");
                if (!(firstAchievement && secondAchievement)) {
                    return firstAchievement ? 1 : -1;
                }

                return first.getPrettyStat().compareTo(second.getPrettyStat());
            });

            for (final JavaStat stat : stats) {
                this.sortedStatsNames.add(stat.getName());
            }
        }

        return this.sortedStatsNames;
    }

    public List<JavaStat> getSimilarStats(final String name, final double similarity, final int limit) {
        final List<JavaStat> similarGames = new ArrayList<>();

        final String[] similarCommandNames = DataUtilities.getSimilarityList(name, this.stats.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGames.add(this.stats.get(similarCommandNames[index]));
        }

        return similarGames;
    }
}