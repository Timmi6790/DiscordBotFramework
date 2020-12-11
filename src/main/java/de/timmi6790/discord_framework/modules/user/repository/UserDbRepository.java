package de.timmi6790.discord_framework.modules.user.repository;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.NonNull;

import java.util.Optional;

public interface UserDbRepository {
    UserDb create(long discordId);

    Optional<UserDb> get(long discordId);

    void delete(@NonNull UserDb userDb);

    void setBanStatus(int userDatabaseId, boolean isBanned);

    void addPermission(int userDatabaseId, int permissionId);

    void removePermission(int userDatabaseId, int permissionId);

    void setPrimaryRank(int userDatabaseId, int rankId);

    void addRank(int userDatabaseId, int rankId);

    void removeRank(int userDatabaseId, int rankId);

    void insertStat(int userDatabaseId, int statId, int statValue);

    void updateStat(int userDatabaseId, int statId, int statValue);

    void grantSetting(int userDatabaseId, int settingId, String defaultValue);

    void updateSetting(int userDatabaseId, int settingId, String newValue);

    void grantPlayerAchievement(int playerId, int achievementId);
}
