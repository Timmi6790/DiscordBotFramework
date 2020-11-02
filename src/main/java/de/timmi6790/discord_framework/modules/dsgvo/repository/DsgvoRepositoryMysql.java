package de.timmi6790.discord_framework.modules.dsgvo.repository;

import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.dsgvo.DsgvoModule;
import de.timmi6790.discord_framework.modules.dsgvo.UserData;
import org.jdbi.v3.core.Jdbi;

public class DsgvoRepositoryMysql implements DsgvoRepository {
    private static final String GET_USER_DATA = "SELECT player.discordId, player.register_date registerDate, player.shop_points shopPoints, player.banned isBanned, mainRank.rank_name mainRank, " +
            "GROUP_CONCAT(DISTINCT CONCAT(achievement.achievement_name, ':', pachievement.date)) achievements, " +
            "GROUP_CONCAT(DISTINCT CONCAT(stat.stat_name, ':', pstat.`value`)) stats, " +
            "GROUP_CONCAT(DISTINCT rank.rank_name) secondaryRanks, " +
            "GROUP_CONCAT(DISTINCT CONCAT(setting.setting_name, ':', psetting.setting)) settings, " +
            "GROUP_CONCAT(DISTINCT permission.permission_node) playerPermissions " +
            "FROM  player " +
            "INNER JOIN rank mainRank ON mainRank.id = player.primary_rank " +
            "LEFT JOIN player_achievement pachievement ON pachievement.player_id = player.id " +
            "LEFT JOIN achievement ON achievement.id = pachievement.achievement_id " +
            "LEFT JOIN player_stat pstat ON pstat.player_id = player.id " +
            "LEFT JOIN stat ON stat.id = pstat.stat_id " +
            "LEFT JOIN player_rank prank ON prank.player_id = player.id " +
            "LEFT JOIN rank ON rank.id = prank.rank_id " +
            "LEFT JOIN player_setting psetting ON psetting.player_id = player.id " +
            "LEFT JOIN setting ON setting.id = psetting.setting_id " +
            "LEFT JOIN player_permission ppermission ON ppermission.player_id = player.id " +
            "LEFT JOIN permission ON permission.id = ppermission.permission_id " +
            "WHERE player.discordId = :discordUserId " +
            "GROUP BY player.id;";

    private final Jdbi database;

    public DsgvoRepositoryMysql(final DsgvoModule module) {
        this.database = module.getModuleOrThrow(DatabaseModule.class).getJdbi();
        this.database.registerRowMapper(new UserDataDatabaseMapper());
    }

    @Override
    public UserData getUserData(final long discordUserId) {
        return this.database.withHandle(handle -> handle.createQuery(GET_USER_DATA)
                .bind("discordUserId", discordUserId)
                .mapTo(UserData.class)
                .first()
        );
    }
}
