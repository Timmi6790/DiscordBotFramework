package de.timmi6790.statsbotdiscord.modules.achievement;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementManager {
    @Getter
    private final Map<Integer, AbstractAchievement> achievements = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public void registerAchievements(final AbstractAchievement... achievements) {
        Arrays.stream(achievements)
                .filter(abstractAchievement -> !this.achievements.containsKey(abstractAchievement.getDatabaseId()))
                .forEach(achievement -> {
                    this.achievements.put(achievement.getDatabaseId(), achievement);
                    this.nameIdMatching.put(achievement.getInternalName(), achievement.getDatabaseId());

                    StatsBot.getEventManager().addEventListener(achievement);
                });
    }

    public void grantAchievement(final UserDb userDb, final AbstractAchievement achievement) {
        userDb.grantAchievement(achievement);
    }
}
