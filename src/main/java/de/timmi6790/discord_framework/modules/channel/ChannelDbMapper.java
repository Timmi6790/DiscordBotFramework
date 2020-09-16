package de.timmi6790.discord_framework.modules.channel;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class ChannelDbMapper extends DatabaseRowMapper implements RowMapper<ChannelDb> {
    private final GuildDbModule guildDbModule;

    @Override
    public ChannelDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        if (rs.getInt("id") == 0) {
            return null;
        }

        return new ChannelDb(
                rs.getInt("id"),
                rs.getLong("discordId"),
                this.guildDbModule.getOrCreate(rs.getLong("serverDiscordId")),
                rs.getBoolean("disabled")
        );
    }
}