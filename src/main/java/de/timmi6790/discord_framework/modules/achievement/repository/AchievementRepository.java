package de.timmi6790.discord_framework.modules.achievement.repository;

/**
 * Achievement repository
 */
public interface AchievementRepository {
    /**
     * Retrieves the repository id for the given achievement name or creates one.
     *
     * @param internalAchievementName the internal achievement name
     * @return the repository id of the achievement
     */
    int retrieveOrCreateAchievementId(String internalAchievementName);
}
