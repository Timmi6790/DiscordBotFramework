package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.utilities;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;

import java.util.*;

public class StatsComparator implements Comparator<JavaStat> {
    private static final List<String> STATS_ORDER = new ArrayList<>(Arrays.asList("Wins", "TimeInGame", "TimeInHub", "GamesPlayed", "DailyVote", "ClansDailyReward", "DailyReward",
            "CrownsEarned", "BestWinStreak", "SecondPlace", "ThirdPlace", "Losses", "Kills", "FinalKills", "Assists", "Deaths"));

    static {
        Collections.reverse(STATS_ORDER);
    }

    @Override
    public int compare(final JavaStat o1, final JavaStat o2) {
        if (o1.equals(o2)) {
            return 0;
        }

        final int firstIndex = STATS_ORDER.indexOf(o1.getName());
        final int secondIndex = STATS_ORDER.indexOf(o2.getName());
        if (Math.max(secondIndex, firstIndex) != -1) {
            return Integer.compare(secondIndex, firstIndex);
        }

        final boolean firstAchievement = o1.getPrettyStat().startsWith("Achievement");
        final boolean secondAchievement = o2.getPrettyStat().startsWith("Achievement");
        if (!(firstAchievement && secondAchievement)) {
            return firstAchievement ? 1 : -1;
        }

        return o1.getPrettyStat().compareTo(o2.getPrettyStat());
    }
}
