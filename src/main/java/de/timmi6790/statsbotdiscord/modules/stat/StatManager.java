package de.timmi6790.statsbotdiscord.modules.stat;

import de.timmi6790.statsbotdiscord.StatsBot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatManager {
    private final Map<Integer, AbstractStat> stats = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public void registerSetting(final AbstractStat setting) {

        StatsBot.getEventManager().addEventListener(setting);
    }
}
