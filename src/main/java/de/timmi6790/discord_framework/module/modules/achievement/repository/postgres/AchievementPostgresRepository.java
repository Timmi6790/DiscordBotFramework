package de.timmi6790.discord_framework.module.modules.achievement.repository.postgres;

import de.timmi6790.discord_framework.module.modules.achievement.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

/**
 * Postgres achievement repository implementation
 */
@RequiredArgsConstructor
public class AchievementPostgresRepository implements AchievementRepository {
    private static final String INSERT_ACHIEVEMENT = "INSERT INTO achievements(achievement_name) VALUES(:achievementName) RETURNING id;";

    private final Jdbi database;

    @Override
    public int retrieveOrCreateAchievementId(final String internalAchievementName) {
        return this.database.withHandle(handle ->
                handle.createQuery(INSERT_ACHIEVEMENT)
                        .bind("achievementName", internalAchievementName)
                        .mapTo(Integer.class)
                        .first()
        );
    }
}
