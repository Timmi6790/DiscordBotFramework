package de.timmi6790.discord_framework.modules.user.repository;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class UserDbMapper extends DatabaseRowMapper implements RowMapper<UserDb> {
    private final UserDbRepository userDbRepository;

    @Override
    public UserDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        if (rs.getInt("id") == 0) {
            return null;
        }

        return new UserDb(
                this.userDbRepository,
                rs.getInt("id"),
                rs.getLong("discordId"),
                rs.getInt("primaryRank"),
                this.toSet(rs.getString("ranks"), Integer::parseInt),
                rs.getBoolean("banned"),
                rs.getLong("shopPoints"),
                this.toSet(rs.getString("perms"), Integer::parseInt),
                this.toMap(rs.getString("settings"), Integer::parseInt, String::valueOf),
                this.toMap(rs.getString("stats"), Integer::parseInt, Integer::parseInt),
                this.toSet(rs.getString("achievements"), Integer::parseInt)
        );
    }
}