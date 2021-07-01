package de.timmi6790.discord_framework.module.modules.channel.repository.postgres.mappers;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps the database row to {@link ChannelDb}
 */
@AllArgsConstructor
public class ChannelDbMapper extends DatabaseRowMapper implements RowMapper<ChannelDb> {
    private final GuildDbModule guildDbModule;
    private final ShardManager discord;

    @Override
    public ChannelDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new ChannelDb(
                this.guildDbModule.getOrCreate(rs.getLong("guild_id")),
                this.discord,
                rs.getLong("discord_id"),
                rs.getBoolean("disabled")
        );
    }
}