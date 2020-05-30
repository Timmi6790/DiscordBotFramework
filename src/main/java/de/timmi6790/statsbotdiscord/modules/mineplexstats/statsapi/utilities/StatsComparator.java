package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.utilities;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;

import java.util.*;

public class StatsComparator implements Comparator<JavaStat> {
    private static final List<String> STATS_ORDER = new ArrayList<>(Arrays.asList("Hider Wins", "Hunter Wins", "Wins", "TimeInGame", "TimeInHub", "GamesPlayed",
            "DailyVote", "ClansDailyReward", "DailyReward", "CrownsEarned", "BestWinStreak", "SecondPlace", "ThirdPlace", "Losses", "Kills", "FinalKills", "Assists", "Deaths"));

    static {
        Collections.reverse(STATS_ORDER);
    }

    @Override
    public int compare(final JavaStat object1, final JavaStat object2) {
        if (object1.equals(object2)) {
            return 0;
        }

        final int firstIndex = STATS_ORDER.indexOf(JavaGame.getCleanStat(object1.getPrintName()));
        final int secondIndex = STATS_ORDER.indexOf(JavaGame.getCleanStat(object2.getPrintName()));
        if (Math.max(secondIndex, firstIndex) != -1) {
            return Integer.compare(secondIndex, firstIndex);
        }

        final boolean firstAchievement = object1.getPrintName().startsWith("Achievement");
        final boolean secondAchievement = object2.getPrintName().startsWith("Achievement");
        if (!(firstAchievement && secondAchievement)) {
            return firstAchievement ? 1 : -1;
        }

        return object1.getPrintName().compareTo(object2.getPrintName());
    }
}
