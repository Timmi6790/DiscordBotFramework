package de.timmi6790.discord_framework.module.modules.user.repository;

import de.timmi6790.discord_framework.module.modules.user.UserDb;

import java.util.Optional;

public interface UserDbRepository {
    UserDb create(long userId);

    Optional<UserDb> get(long userId);

    void delete(long userId);

    void setBanStatus(long userId, boolean isBanned);

    void addPermission(long userId, int permissionId);

    void removePermission(long userId, int permissionId);

    void setPrimaryRank(long userId, int rankId);

    void addRank(long userId, int rankId);

    void removeRank(long userId, int rankId);

    void insertStat(long userId, int statId, int statValue);

    void updateStat(long userId, int statId, int statValue);

    void grantSetting(long userId, int settingId, String defaultValue);

    void updateSetting(long userId, int settingId, String newValue);

    void grantAchievement(long userId, int achievementId);
}
