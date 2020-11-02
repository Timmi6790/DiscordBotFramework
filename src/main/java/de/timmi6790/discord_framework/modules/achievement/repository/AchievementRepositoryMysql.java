package de.timmi6790.discord_framework.modules.achievement.repository;

import de.timmi6790.commons.builders.MapBuilder;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;

public class AchievementRepositoryMysql implements AchievementRepository {
    private static final String ACHIEVEMENT_NAME = "achievementName";

    private static final String GET_ACHIEVEMENT_ID = "SELECT id FROM `achievement` WHERE achievement_name = :achievementName LIMIT 1;";
    private static final String INSERT_NEW_ACHIEVEMENT = "INSERT INTO achievement(achievement_name) VALUES(:achievementName);";

    private static final String INSERT_PLAYER_ACHIEVEMENT = "INSERT player_achievement(player_id, achievement_id) VALUES(:playerId, :achievementId)";

    private final DatabaseModule databaseModule;

    public AchievementRepositoryMysql(final AchievementModule module) {
        this.databaseModule = module.getModuleOrThrow(DatabaseModule.class);
    }

    @Override
    public int retrieveOrCreateSettingId(final String internalName) {
        return this.databaseModule.retrieveOrCreateId(
                GET_ACHIEVEMENT_ID,
                MapBuilder.<String, Object>ofHashMap()
                        .put(ACHIEVEMENT_NAME, internalName)
                        .build(),
                INSERT_NEW_ACHIEVEMENT,
                MapBuilder.<String, Object>ofHashMap()
                        .put(ACHIEVEMENT_NAME, internalName)
                        .build()
        );
    }

    @Override
    public void grantPlayerAchievement(final int playerId, final int achievementId) {
        this.databaseModule.getJdbi().useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_ACHIEVEMENT)
                        .bind("playerId", playerId)
                        .bind("achievementId", achievementId)
                        .execute()
        );
    }
}
