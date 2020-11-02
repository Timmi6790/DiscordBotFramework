package de.timmi6790.discord_framework.modules.rank.repository;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class RankMapper extends DatabaseRowMapper implements RowMapper<Rank> {
    private final RankModule rankModule;
    private final UserDbModule userDbModule;

    @Override
    public Rank map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new Rank(
                this.rankModule,
                this.userDbModule,
                rs.getInt("id"),
                rs.getString("rankName"),
                this.toSet(rs.getString("parentRanks"), Integer::parseInt),
                this.toSet(rs.getString("permissions"), Integer::parseInt)
        );
    }
}
