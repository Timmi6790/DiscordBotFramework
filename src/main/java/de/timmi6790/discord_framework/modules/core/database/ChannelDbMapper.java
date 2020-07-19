package de.timmi6790.discord_framework.modules.core.database;

import de.timmi6790.discord_framework.modules.core.ChannelDb;
import de.timmi6790.discord_framework.modules.core.GuildDb;
import de.timmi6790.discord_framework.utilities.database.DatabaseRowMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelDbMapper extends DatabaseRowMapper implements RowMapper<ChannelDb> {
    @Override
    public ChannelDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        if (rs.getInt("id") == 0) {
            return null;
        }

        return new ChannelDb(
                rs.getInt("id"),
                rs.getLong("discordId"),
                GuildDb.getOrCreate(rs.getLong("serverDiscordId")),
                rs.getBoolean("disabled")
        );
    }
}
