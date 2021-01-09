package de.timmi6790.discord_framework.modules.guild.repository.mysql.mappers;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@AllArgsConstructor
public class GuildDbDatabaseMapper extends DatabaseRowMapper implements RowMapper<GuildDb> {
    private final ShardManager discord;

    @Override
    public GuildDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        if (rs.getInt("id") == 0) {
            return null;
        }

        return new GuildDb(
                this.discord,
                rs.getInt("id"),
                rs.getLong("discordId"),
                rs.getBoolean("banned"),
                new HashMap<>()
        );
    }
}