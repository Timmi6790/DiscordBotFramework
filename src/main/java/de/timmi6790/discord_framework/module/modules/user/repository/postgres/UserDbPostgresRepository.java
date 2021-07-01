package de.timmi6790.discord_framework.module.modules.user.repository.postgres;

import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.stat.StatModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import de.timmi6790.discord_framework.module.modules.user.repository.UserDbRepository;
import de.timmi6790.discord_framework.module.modules.user.repository.postgres.mappers.UserDbMapper;
import de.timmi6790.discord_framework.module.modules.user.repository.postgres.reducers.UserDbReducer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class UserDbPostgresRepository implements UserDbRepository {
    private static final String USER_ID = "userId";
    private static final String DATABASE_ID = "databaseId";

    private static final String GET_PLAYER = "SELECT \"user\".discord_id, \"user\".banned, \"user\".primary_rank_id, u_rank.rank_id, u_perm.permission_id, u_setting.setting_id, u_setting.setting setting_value, u_stat.stat_id, u_stat.value stat_value, u_ach.achievement_id "
            + "FROM \"user\".users \"user\" "
            + "LEFT JOIN \"user\".user_ranks u_rank ON u_rank.user_id = \"user\".discord_id  "
            + "LEFT JOIN \"user\".user_permissions u_perm ON u_perm.user_id = \"user\".discord_id  "
            + "LEFT JOIN \"user\".user_settings u_setting ON u_setting.user_id = \"user\".discord_id  "
            + "LEFT JOIN \"user\".user_stats u_stat ON u_stat.user_id = \"user\".discord_id "
            + "LEFT JOIN \"user\".user_achievements u_ach ON u_ach.user_id = \"user\".discord_id "
            + "WHERE \"user\".discord_id = :discordId;";

    private static final String INSERT_PLAYER = "INSERT INTO \"user\".users(discord_id) VALUES (:discordId);";
    private static final String REMOVE_PLAYER = "DELETE FROM \"user\".users WHERE discord_id = :dbId;";
    private static final String UPDATE_PLAYER_BAN_STATUS = "UPDATE \"user\".users SET banned = :banned WHERE discord_id = :databaseId;";

    private static final String UPDATE_STAT_VALUE = "UPDATE \"user\".user_stats SET \"value\" = :value WHERE user_id = :userId AND stat_id = :statId;";
    private static final String INSERT_STAT_VALUE = "INSERT INTO \"user\".user_stats(user_id, stat_id, value) VALUES(:userId, :statId, :value)";

    private static final String INSERT_PLAYER_SETTING = "INSERT INTO \"user\".user_settings(user_id, setting_id, setting) VALUES(:userId, :settingId, :setting);";
    private static final String UPDATE_PLAYER_SETTING = "UPDATE \"user\".user_settings SET setting = :setting WHERE user_id = :userId and setting_id = :settingId;";

    private static final String INSERT_PLAYER_PERMISSION = "INSERT INTO \"user\".user_permissions(user_id, permission_id) VALUES(:userId, :permissionId);";
    private static final String DELETE_PLAYER_PERMISSION = "DELETE FROM \"user\".user_permissions WHERE user_id = :userId AND permission_id = :permissionId";

    private static final String SET_PRIMARY_RANK = "UPDATE \"user\".users SET primary_rank_id = :primaryRank WHERE discord_id = :databaseId;";
    private static final String ADD_RANK = "INSERT INTO \"user\".user_ranks(user_id, rank_id) VALUES(:databaseId, :rankId);";
    private static final String DELETE_RANK = "DELETE FROM \"user\".user_ranks WHERE user_id = :databaseId AND rank_id = :rankId;";

    private static final String INSERT_PLAYER_ACHIEVEMENT = "INSERT INTO \"user\".user_achievements(user_id, achievement_id) VALUES(:userId, :achievementId)";

    private final Jdbi database;

    private final RankModule rankModule;
    private final @Nullable AchievementModule achievementModule;
    private final @Nullable SettingModule settingModule;
    private final @Nullable StatModule statModule;

    public UserDbPostgresRepository(final UserDbModule userDbModule,
                                    final DatabaseModule databaseModule,
                                    final EventModule eventModule,
                                    final RankModule rankModule,
                                    @Nullable final AchievementModule achievementModule,
                                    @Nullable final SettingModule settingModule,
                                    @Nullable final StatModule statModule) {
        this.database = databaseModule.getJdbi();
        this.database.registerRowMapper(
                new UserDbMapper(
                        userDbModule,
                        eventModule,
                        rankModule,
                        settingModule
                )
        );

        this.rankModule = rankModule;
        this.achievementModule = achievementModule;
        this.settingModule = settingModule;
        this.statModule = statModule;
    }

    private UserDbReducer getUserDbReducer() {
        return new UserDbReducer(
                this.rankModule,
                this.achievementModule,
                this.settingModule,
                this.statModule
        );
    }

    @Override
    public UserDb create(final long userId) {
        // Make sure that the user is not present
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER)
                        .bind("discordId", userId)
                        .execute()
        );

        // Should never throw
        return this.get(userId).orElseThrow(RuntimeException::new);
    }

    @Override
    public Optional<UserDb> get(final long userId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_PLAYER)
                        .bind("discordId", userId)
                        .reduceRows(this.getUserDbReducer())
                        .findFirst()
        );
    }

    @Override
    public void delete(final long userId) {
        this.database.useHandle(handle ->
                handle.createUpdate(REMOVE_PLAYER)
                        .bind("dbId", userId)
                        .execute()
        );
    }

    @Override
    public void setBanStatus(final long userId, final boolean isBanned) {
        this.database.useHandle(handle ->
                handle.createUpdate(UPDATE_PLAYER_BAN_STATUS)
                        .bind("banned", isBanned)
                        .bind(DATABASE_ID, userId)
                        .execute()
        );
    }

    @Override
    public void addPermission(final long userId, final int permissionId) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_PERMISSION)
                        .bind(USER_ID, userId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
    }

    @Override
    public void removePermission(final long userId, final int permissionId) {
        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_PLAYER_PERMISSION)
                        .bind(USER_ID, userId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
    }

    @Override
    public void setPrimaryRank(final long userId, final int rankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(SET_PRIMARY_RANK)
                        .bind("primaryRank", rankId)
                        .bind(DATABASE_ID, userId)
                        .execute()
        );
    }

    @Override
    public void addRank(final long userId, final int rankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(ADD_RANK)
                        .bind("rankId", rankId)
                        .bind(DATABASE_ID, userId)
                        .execute()
        );
    }

    @Override
    public void removeRank(final long userId, final int rankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_RANK)
                        .bind("rankId", rankId)
                        .bind(DATABASE_ID, userId)
                        .execute()
        );
    }

    @Override
    public void insertStat(final long userId, final int statId, final int statValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_STAT_VALUE)
                        .bind(USER_ID, userId)
                        .bind("statId", statId)
                        .bind("value", statValue)
                        .execute()
        );
    }

    @Override
    public void updateStat(final long userId, final int statId, final int statValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(UPDATE_STAT_VALUE)
                        .bind(USER_ID, userId)
                        .bind("statId", statId)
                        .bind("value", statValue)
                        .execute()
        );
    }

    @Override
    public void grantSetting(final long userId, final int settingId, final String defaultValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_SETTING)
                        .bind(USER_ID, userId)
                        .bind("settingId", settingId)
                        .bind("setting", defaultValue)
                        .execute()
        );
    }

    @Override
    public void updateSetting(final long userId, final int settingId, final String newValue) {
        this.database.useHandle(handle ->
                handle.createUpdate(UPDATE_PLAYER_SETTING)
                        .bind(USER_ID, userId)
                        .bind("settingId", settingId)
                        .bind("setting", newValue)
                        .execute()
        );
    }

    @Override
    public void grantAchievement(final long userId, final int achievementId) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_ACHIEVEMENT)
                        .bind(USER_ID, userId)
                        .bind("achievementId", achievementId)
                        .execute()
        );
    }
}
