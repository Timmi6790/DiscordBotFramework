package de.timmi6790.discord_framework.modules.user.repository;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.NonNull;

import java.util.Optional;

public interface UserDbRepository {
    UserDb create(final long discordId);

    Optional<UserDb> get(final long discordId);

    void delete(@NonNull final UserDb userDb);

    void setBanStatus(final int userDatabaseId, final boolean isBanned);

    void addPermission(int userDatabaseId, int permissionId);

    void removePermission(int userDatabaseId, int permissionId);

    void setPrimaryRank(int userDatabaseId, final int rankId);

    void addRank(int userDatabaseId, final int rankId);

    void removeRank(int userDatabaseId, final int rankId);

    void insertStat(int userDatabaseId, int statId, int statValue);

    void updateStat(int userDatabaseId, int statId, int statValue);

    void grantSetting(int userDatabaseId, int settingId, String defaultValue);
}
