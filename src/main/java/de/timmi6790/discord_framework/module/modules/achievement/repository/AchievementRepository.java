package de.timmi6790.discord_framework.module.modules.achievement.repository;

import java.util.Optional;

/**
 * Achievement repository
 */
public interface AchievementRepository {
    Optional<Integer> getAchievementId(String internalAchievementName);

    /**
     * Retrieves the repository id for the given achievement name or creates one.
     *
     * @param internalAchievementName the internal achievement name
     * @return the repository id of the achievement
     */
    int createAchievement(String internalAchievementName);
}
