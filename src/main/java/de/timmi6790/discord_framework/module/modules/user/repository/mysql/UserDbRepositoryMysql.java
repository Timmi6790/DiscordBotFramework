package de.timmi6790.discord_framework.module.modules.user.repository.mysql;

import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.stat.StatModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import de.timmi6790.discord_framework.module.modules.user.repository.UserDbRepository;
import de.timmi6790.discord_framework.module.modules.user.repository.mysql.mappers.UserDbDatabaseMapper;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class UserDbRepositoryMysql implements UserDbRepository {
    private static final String PLAYER_ID = "playerId";
    private static final String DATABASE_ID = "databaseId";

    private static final String GET_PLAYER = "SELECT player.id, player.discordId, player.banned, player.primary_rank primaryRank, "
            + "GROUP_CONCAT(DISTINCT p_rank.rank_id) ranks, "
            + "GROUP_CONCAT(DISTINCT permission.id) perms, "
            + "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_setting.setting_id, p_setting.setting) SEPARATOR ';') settings, "
            + "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_stat.stat_id, p_stat.value) SEPARATOR ';') stats, "
            + "GROUP_CONCAT(DISTINCT p_ach.achievement_id) achievements "
            + "FROM player "
            + "LEFT JOIN player_rank p_rank ON p_rank.player_id = player.id  "
            + "LEFT JOIN player_permission p_perm ON p_perm.player_id = player.id  "
            + "LEFT JOIN permission ON permission.id = p_perm.permission_id  "
            + "LEFT JOIN player_setting p_setting ON p_setting.player_id = player.id  "
            + "LEFT JOIN player_stat p_stat ON p_stat.player_id = player.id "
            + "LEFT JOIN player_achievement p_ach ON p_ach.player_id = player.id "
            + "WHERE player.discordId = :discordId LIMIT 1;";
    private static final String INSERT_PLAYER = "INSERT INTO player(discordId) VALUES (:discordId);";
    private static final String REMOVE_PLAYER = "DELETE FROM player WHERE id = :dbId LIMIT 1;";
    private static final String UPDATE_PLAYER_BAN_STATUS = "UPDATE player SET banned = :banned WHERE id = :databaseId LIMIT 1;";

    private static final String UPDATE_STAT_VALUE = "UPDATE player_stat SET `value` = :value WHERE player_id = :playerId AND stat_id = :statId LIMIT 1;";
    private static final String INSERT_STAT_VALUE = "INSERT player_stat(player_id, stat_id, value) VALUES(:playerId, :statId, :value)";

    private static final String INSERT_PLAYER_SETTING = "INSERT player_setting(player_id, setting_id, setting) VALUES(:playerId, :settingId, :setting);";
    private static final String UPDATE_PLAYER_SETTING = "UPDATE player_setting SET setting = :setting WHERE player_id = :playerId and setting_id = :settingId LIMIT 1;";

    private static final String INSERT_PLAYER_PERMISSION = "INSERT INTO player_permission(player_id, permission_id) VALUES(:playerId, :permissionId);";
    private static final String DELETE_PLAYER_PERMISSION = "DELETE FROM player_permission WHERE player_id = :playerId AND permission_id = :permissionId LIMIT 1";

    private static final String SET_PRIMARY_RANK = "UPDATE `player` SET player.primary_rank = :primaryRank WHERE player.id = :databaseId LIMIT 1;";
    private static final String ADD_RANK = "INSERT INTO player_rank(player_id, rank_id) VALUES(:databaseId, :rankId);";
    private static final String DELETE_RANK = "DELETE FROM player_rank WHERE player_rank.player_id = :databaseId AND player_rank.rank_id = :rankId LIMIT 1;";

    private static final String INSERT_PLAYER_ACHIEVEMENT = "INSERT player_achievement(player_id, achievement_id) VALUES(:playerId, :achievementId)";

    private final Jdbi database;

    public UserDbRepositoryMysql(final UserDbModule userDbModule,
                                 final DatabaseModule databaseModule,
                                 final EventModule eventModule,
                                 final RankModule rankModule,
                                 @Nullable final AchievementModule achievementModule,
                                 @Nullable final SettingModule settingModule,
                                 @Nullable final StatModule statModule) {
        this.database = databaseModule.getJdbi();
        this.database.registerRowMapper(
                UserDb.class,
                new UserDbDatabaseMapper(
                        userDbModule,
                        eventModule,
                        rankModule,
                        achievementModule,
                        settingModule,
                        statModule
                )
        );
    }

    @Override
    public UserDb create(final long discordId) {
        // Make sure that the user is not present
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER)
                        .bind("discordId", discordId)
                        .execute()
        );

        // Should never throw
        return this.get(discordId).orElseThrow(RuntimeException::new);
    }

    @Override
    public Optional<UserDb> get(final long discordId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_PLAYER)
                        .bind("discordId", discordId)
                        .mapTo(UserDb.class)
                        .findFirst()
        );
    }

    @Override
    public void delete(@NonNull final UserDb userDb) {
        this.database.useHandle(handle ->
                handle.createUpdate(REMOVE_PLAYER)
                        .bind("dbId", userDb.getDatabaseId())
                        .execute()
        );
    }

    @Override
    public void setBanStatus(final int userDatabaseId, final boolean isBanned) {
        this.database.useHandle(handle ->
                handle.createUpdate(UPDATE_PLAYER_BAN_STATUS)
                        .bind("banned", isBanned ? 1 : 0)
                        .bind(DATABASE_ID, userDatabaseId)
                        .execute()
        );
    }

    @Override
    public void addPermission(final int userDatabaseId, final int permissionId) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_PERMISSION)
                        .bind(PLAYER_ID, userDatabaseId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
    }

    @Override
    public void removePermission(final int userDatabaseId, final int permissionId) {
        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_PLAYER_PERMISSION)
                        .bind(PLAYER_ID, userDatabaseId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
    }

    @Override
    public void setPrimaryRank(final int userDatabaseId, final int rankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(SET_PRIMARY_RANK)
                        .bind("primaryRank", rankId)
                        .bind(DATABASE_ID, userDatabaseId)
                        .execute()
        );
    }

    @Override
    public void addRank(final int userDatabaseId, final int rankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(ADD_RANK)
                        .bind("rankId", rankId)
                        .bind(DATABASE_ID, userDatabaseId)
                        .execute()
        );
    }

    @Override
    public void removeRank(final int userDatabaseId, final int rankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_RANK)
                        .bind("rankId", rankId)
                        .bind(DATABASE_ID, userDatabaseId)
                        .execute()
        );
    }

    @Override
    public void insertStat(final int userDatabaseId, final int statId, final int statValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_STAT_VALUE)
                        .bind(PLAYER_ID, userDatabaseId)
                        .bind("statId", statId)
                        .bind("value", statValue)
                        .execute()
        );
    }

    @Override
    public void updateStat(final int userDatabaseId, final int statId, final int statValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(UPDATE_STAT_VALUE)
                        .bind(PLAYER_ID, userDatabaseId)
                        .bind("statId", statId)
                        .bind("value", statValue)
                        .execute()
        );
    }

    @Override
    public void grantSetting(final int userDatabaseId, final int settingId, final String defaultValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_SETTING)
                        .bind(PLAYER_ID, userDatabaseId)
                        .bind("settingId", settingId)
                        .bind("setting", defaultValue)
                        .execute()
        );
    }

    @Override
    public void updateSetting(final int userDatabaseId, final int settingId, final String newValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(UPDATE_PLAYER_SETTING)
                        .bind(PLAYER_ID, userDatabaseId)
                        .bind("settingId", settingId)
                        .bind("setting", newValue)
                        .execute()
        );
    }

    @Override
    public void grantPlayerAchievement(final int playerId, final int achievementId) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_ACHIEVEMENT)
                        .bind(PLAYER_ID, playerId)
                        .bind("achievementId", achievementId)
                        .execute()
        );
    }
}
