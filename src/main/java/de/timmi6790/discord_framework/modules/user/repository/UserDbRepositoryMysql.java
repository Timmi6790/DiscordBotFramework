package de.timmi6790.discord_framework.modules.user.repository;

import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class UserDbRepositoryMysql implements UserDbRepository {
    private static final String GET_PLAYER = "SELECT player.id, player.discordId, player.shop_points shopPoints, player.banned, player.primary_rank primaryRank, " +
            "GROUP_CONCAT(DISTINCT p_rank.rank_id) ranks, " +
            "GROUP_CONCAT(DISTINCT permission.id) perms, " +
            "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_setting.setting_id, p_setting.setting) SEPARATOR ';') settings, " +
            "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_stat.stat_id, p_stat.value) SEPARATOR ';') stats, " +
            "GROUP_CONCAT(DISTINCT p_ach.achievement_id) achievements " +
            "FROM player  " +
            "LEFT JOIN player_rank p_rank ON p_rank.player_id = player.id  " +
            "LEFT JOIN player_permission p_perm ON p_perm.player_id = player.id  " +
            "LEFT JOIN permission ON permission.default_permission = 1 OR permission.id = p_perm.permission_id  " +
            "LEFT JOIN player_setting p_setting ON p_setting.player_id = player.id  " +
            "LEFT JOIN player_stat p_stat ON p_stat.player_id = player.id " +
            "LEFT JOIN player_achievement p_ach ON p_ach.player_id = player.id " +
            "WHERE player.discordId = :discordId LIMIT 1;";
    private static final String INSERT_PLAYER = "INSERT INTO player(discordId) VALUES (:discordId);";
    private static final String REMOVE_PLAYER = "DELETE FROM player WHERE id = :dbId LIMIT 1;";

    private final Jdbi database;

    public UserDbRepositoryMysql(final UserDbModule module) {
        this.database = module.getModuleOrThrow(DatabaseModule.class).getJdbi();
        this.database.registerRowMapper(UserDb.class, new UserDbMapper(this.database));
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
}
