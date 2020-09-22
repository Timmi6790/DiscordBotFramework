package de.timmi6790.discord_framework.modules.dsgvo;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import lombok.Data;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

@Data
public class UserData {
    private final long discordId;
    private final @NonNull Timestamp registerDate;
    private final long shopPoints;
    private final boolean isBanned;
    private final @NonNull String mainRank;
    private final @Nullable Map<String, Timestamp> achievements;
    private final @Nullable Map<String, Long> stats;
    private final @Nullable Set<String> secondaryRanks;
    private final @Nullable Map<String, String> settings;
    private final @Nullable Set<String> playerSpecificPermissions;

    public static class DatabaseMapper extends DatabaseRowMapper implements RowMapper<UserData> {
        @Override
        public UserData map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            final Map<String, Timestamp> achievements = this.toMap(rs.getString("achievements"), String::toString, Timestamp::valueOf, ",", ":");
            final Map<String, Long> stats = this.toMap(rs.getString("stats"), String::toString, Long::parseLong, ",", ":");
            final Set<String> secondaryRanks = this.toSet(rs.getString("secondaryRanks"), String::toString);
            final Map<String, String> settings = this.toMap(rs.getString("settings"), String::toString, String::toString, ",", ":");
            final Set<String> playerSpecificPermissions = this.toSet(rs.getString("playerPermissions"), String::toString);

            return new UserData(
                    rs.getLong("discordId"),
                    rs.getTimestamp("registerDate"),
                    rs.getLong("shopPoints"),
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
}
