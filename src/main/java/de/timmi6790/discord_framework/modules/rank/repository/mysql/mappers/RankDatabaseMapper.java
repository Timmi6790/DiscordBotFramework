package de.timmi6790.discord_framework.modules.rank.repository.mysql.mappers;

import de.timmi6790.discord_framework.modules.database.DatabaseRowMapper;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map the repository entry to {@link Rank}.
 */
@AllArgsConstructor
public class RankDatabaseMapper extends DatabaseRowMapper implements RowMapper<Rank> {
    /**
     * The Rank module.
     */
    private final RankModule rankModule;
    /**
     * The User db module.
     */
    private final UserDbModule userDbModule;
    /**
     * The Permissions module.
     */
    private final PermissionsModule permissionsModule;

    @Override
    public Rank map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new Rank(
                this.rankModule,
                this.userDbModule,
                this.permissionsModule,
                rs.getInt("id"),
                rs.getString("rankName"),
                this.toSet(rs.getString("parentRanks"), Integer::parseInt),
                this.toSet(rs.getString("permissions"), Integer::parseInt)
        );
    }
}
