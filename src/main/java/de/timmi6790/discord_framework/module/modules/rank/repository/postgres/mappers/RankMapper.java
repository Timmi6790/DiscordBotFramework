package de.timmi6790.discord_framework.module.modules.rank.repository.postgres.mappers;

import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Map the repository entry to {@link Rank}.
 */
@AllArgsConstructor
public class RankMapper implements RowMapper<Rank> {
    /**
     * The Rank module.
     */
    private final RankModule rankModule;
    /**
     * The Permissions module.
     */
    private final PermissionsModule permissionsModule;

    @Override
    public Rank map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        return new Rank(
                this.rankModule,
                this.permissionsModule,
                rs.getInt("id"),
                rs.getString("rankName"),
                new HashSet<>(),
                new HashSet<>()
        );
    }
}
