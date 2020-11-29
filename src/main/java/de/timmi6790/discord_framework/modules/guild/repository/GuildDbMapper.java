package de.timmi6790.discord_framework.modules.guild.repository;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@AllArgsConstructor
public class GuildDbMapper extends DatabaseRowMapper implements RowMapper<GuildDb> {
    private final JDA discord;

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