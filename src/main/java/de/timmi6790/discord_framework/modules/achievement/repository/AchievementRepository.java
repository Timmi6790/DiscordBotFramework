package de.timmi6790.discord_framework.modules.achievement.repository;

public interface AchievementRepository {
    int retrieveOrCreateSettingId(String internalName);
}
