package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.utilities;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;

import java.util.*;

public class StatsComparator implements Comparator<JavaStat> {
    private static final List<String> STATS_ORDER = new ArrayList<>(Arrays.asList("Hider Wins", "Hunter Wins", "Wins", "TimeInGame", "TimeInHub", "GamesPlayed", "DailyVote", "ClansDailyReward", "DailyReward",
            "CrownsEarned", "BestWinStreak", "SecondPlace", "ThirdPlace", "Losses", "Kills", "FinalKills", "Assists", "Deaths"));

    static {
        Collections.reverse(STATS_ORDER);
    }

    @Override
    public int compare(final JavaStat o1, final JavaStat o2) {
        if (o1.equals(o2)) {
            return 0;
        }

        final int firstIndex = STATS_ORDER.indexOf(JavaGame.getCleanStat(o1.getPrintName()));
        final int secondIndex = STATS_ORDER.indexOf(JavaGame.getCleanStat(o2.getPrintName()));
        if (Math.max(secondIndex, firstIndex) != -1) {
            return Integer.compare(secondIndex, firstIndex);
        }

        final boolean firstAchievement = o1.getPrintName().startsWith("Achievement");
        final boolean secondAchievement = o2.getPrintName().startsWith("Achievement");
        if (!(firstAchievement && secondAchievement)) {
            return firstAchievement ? 1 : -1;
        }

        return o1.getPrintName().compareTo(o2.getPrintName());
    }
}
