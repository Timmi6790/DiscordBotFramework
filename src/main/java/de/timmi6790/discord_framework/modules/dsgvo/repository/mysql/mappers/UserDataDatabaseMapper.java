package de.timmi6790.discord_framework.modules.dsgvo.repository.mysql.mappers;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.modules.dsgvo.UserData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

public class UserDataDatabaseMapper extends DatabaseRowMapper implements RowMapper<UserData> {
    @Override
    public UserData map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        final Map<String, Timestamp> achievements = this.toMap(
                rs.getString("achievements"),
                String::toString,
                Timestamp::valueOf,
                ",",
                ":"
        );
        final Map<String, Long> stats = this.toMap(
                rs.getString("stats"),
                String::toString,
                Long::parseLong,
                ",",
                ":"
        );
        final Set<String> secondaryRanks = this.toSet(
                rs.getString("secondaryRanks")
                , String::toString
        );
        final Map<String, String> settings = this.toMap(
                rs.getString("settings"),
                String::toString,
                String::toString,
                ",",
                ":"
        );
        final Set<String> playerSpecificPermissions = this.toSet(
                rs.getString("playerPermissions"),
                String::toString
        );

        return new UserData(
                rs.getLong("discordId"),
                rs.getTimestamp("registerDate"),
                rs.getBoolean("isBanned"),
                rs.getString("mainRank"),
                achievements.isEmpty() ? null : achievements,
                stats.isEmpty() ? null : stats,
                secondaryRanks.isEmpty() ? null : secondaryRanks,
                settings.isEmpty() ? null : settings,
                playerSpecificPermissions.isEmpty() ? null : playerSpecificPermissions
        );
    }
}
