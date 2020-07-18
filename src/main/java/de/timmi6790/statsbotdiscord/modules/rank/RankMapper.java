package de.timmi6790.statsbotdiscord.modules.rank;

import de.timmi6790.statsbotdiscord.utilities.database.DatabaseRowMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RankMapper extends DatabaseRowMapper implements RowMapper<Rank> {
    @Override
    public Rank map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new Rank(
                rs.getInt("id"),
                rs.getString("rankName"),
                this.toSet(rs.getString("parentRanks"), Integer::parseInt),
                this.toSet(rs.getString("permissions"), Integer::parseInt)
        );
    }
}
