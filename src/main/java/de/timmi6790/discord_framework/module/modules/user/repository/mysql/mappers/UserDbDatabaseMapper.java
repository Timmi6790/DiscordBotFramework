package de.timmi6790.discord_framework.module.modules.user.repository.mysql.mappers;

import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.stat.StatModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class UserDbDatabaseMapper extends DatabaseRowMapper implements RowMapper<UserDb> {
    private final UserDbModule userDbModule;
    private final EventModule eventModule;
    private final RankModule rankModule;
    private final @Nullable AchievementModule achievementModule;
    private final @Nullable SettingModule settingModule;
    private final @Nullable StatModule statModule;

    @Override
    public UserDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        if (rs.getInt("id") == 0) {
            return null;
        }

        return new UserDb(
                this.userDbModule,
                this.eventModule,
                this.rankModule,
                this.achievementModule,
                this.settingModule,
                this.statModule,
                rs.getInt("id"),
                rs.getLong("discordId"),
                rs.getInt("primaryRank"),
                this.toSet(rs.getString("ranks"), Integer::parseInt),
                rs.getBoolean("banned"),
                this.toSet(rs.getString("perms"), Integer::parseInt),
                this.toMap(rs.getString("settings"), Integer::parseInt, String::valueOf),
                this.toMap(rs.getString("stats"), Integer::parseInt, Integer::parseInt),
                this.toSet(rs.getString("achievements"), Integer::parseInt)
        );
    }
}