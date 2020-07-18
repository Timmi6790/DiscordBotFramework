package de.timmi6790.statsbotdiscord.modules.core.database;

import de.timmi6790.statsbotdiscord.modules.core.GuildDb;
import de.timmi6790.statsbotdiscord.utilities.database.DatabaseRowMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildDbMapper extends DatabaseRowMapper implements RowMapper<GuildDb> {
    @Override
    public GuildDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new GuildDb(
                rs.getInt("id"),
                rs.getLong("discordId"),
                rs.getBoolean("banned"),
                this.toSet(rs.getString("aliases"), String::valueOf),
                null
        );
    }
}
