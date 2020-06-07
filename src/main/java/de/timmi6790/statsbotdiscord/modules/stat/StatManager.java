package de.timmi6790.statsbotdiscord.modules.stat;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatManager {
    @Getter
    private final Map<Integer, AbstractStat> stats = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public void registerStats(final AbstractStat... stats) {
        Arrays.stream(stats)
                .filter(stat -> !this.stats.containsKey(stat.getDatabaseId()))
                .forEach(stat -> {
                    this.stats.put(stat.getDatabaseId(), stat);
                    this.nameIdMatching.put(stat.getInternalName(), stat.getDatabaseId());

                    StatsBot.getEventManager().addEventListener(stat);
                });
    }

    public void increaseStat(final UserDb userDb, final AbstractStat stat, final int value) {
        userDb.increaseStat(stat, value);
    }
}
