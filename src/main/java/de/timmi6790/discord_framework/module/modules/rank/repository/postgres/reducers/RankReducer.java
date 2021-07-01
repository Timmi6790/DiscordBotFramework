package de.timmi6790.discord_framework.module.modules.rank.repository.postgres.reducers;

import de.timmi6790.discord_framework.module.modules.rank.Rank;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;

import java.util.Map;

public class RankReducer implements LinkedHashMapRowReducer<Integer, Rank> {
    @Override
    public void accumulate(final Map<Integer, Rank> container, final RowView rowView) {
        final Rank rank = container.computeIfAbsent(
                rowView.getColumn("id", Integer.class),
                id -> rowView.getRow(Rank.class)
        );

        final Integer permissionId = rowView.getColumn("permission_id", Integer.class);
        if (permissionId != null) {
            rank.addPermissionRepositoryOnly(permissionId);
        }

        final Integer parentRankId = rowView.getColumn("parent_rank_id", Integer.class);
        if (parentRankId != null) {
            rank.addExtendedRankRepositoryOnly(parentRankId);
        }
    }
}
