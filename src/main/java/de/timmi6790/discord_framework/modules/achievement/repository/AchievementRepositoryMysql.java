package de.timmi6790.discord_framework.modules.achievement.repository;

import de.timmi6790.commons.builders.MapBuilder;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;

/**
 * Mysql achievement repository implementation
 */
public class AchievementRepositoryMysql implements AchievementRepository {
    private static final String ACHIEVEMENT_NAME = "achievementName";

    private static final String GET_ACHIEVEMENT_ID = "SELECT id FROM `achievement` WHERE achievement_name = :achievementName LIMIT 1;";
    private static final String INSERT_NEW_ACHIEVEMENT = "INSERT INTO achievement(achievement_name) VALUES(:achievementName);";

    private final DatabaseModule databaseModule;

    /**
     * Instantiates a new mysql achievement repository.
     *
     * @param databaseModule the database module
     */
    public AchievementRepositoryMysql(final DatabaseModule databaseModule) {
        this.databaseModule = databaseModule;
    }

    @Override
    public int retrieveOrCreateAchievementId(final String internalAchievementName) {
        return this.databaseModule.retrieveOrCreateId(
                GET_ACHIEVEMENT_ID,
                MapBuilder.<String, Object>ofHashMap()
                        .put(ACHIEVEMENT_NAME, internalAchievementName)
                        .build(),
                INSERT_NEW_ACHIEVEMENT,
                MapBuilder.<String, Object>ofHashMap()
                        .put(ACHIEVEMENT_NAME, internalAchievementName)
                        .build()
        );
    }
}
