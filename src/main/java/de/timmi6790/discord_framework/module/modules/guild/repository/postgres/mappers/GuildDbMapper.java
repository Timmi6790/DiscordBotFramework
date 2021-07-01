package de.timmi6790.discord_framework.module.modules.guild.repository.postgres.mappers;

import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class GuildDbMapper implements RowMapper<GuildDb> {
    private final ShardManager discord;

    @Override
    public GuildDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new GuildDb(
                this.discord,
                rs.getLong("discord_id"),
                rs.getBoolean("banned")
        );
    }
}