package de.timmi6790.discord_framework.modules.rank.repository;

import de.timmi6790.discord_framework.modules.rank.Rank;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

public interface RankRepository {
    List<Rank> loadRanks();

    Rank createRank(@NonNull final String name);

    void deleteRank(final int rankId);

    void addPermission(int rankId, final int permissionId);

    void removePermission(int rankId, final int permissionId);

    void addExtendedRank(int rankId, final int extendedRankId);

    void removeExtendedRank(int rankId, final int extendedRankId);

    void setRankName(int rankId, final String name);
    
    Set<Long> retrieveAllPlayerIdsForRank(int rankId);
}
