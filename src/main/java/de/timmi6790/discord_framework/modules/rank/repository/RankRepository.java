package de.timmi6790.discord_framework.modules.rank.repository;

import de.timmi6790.discord_framework.modules.rank.Rank;
import lombok.NonNull;

import java.util.List;

public interface RankRepository {
    List<Rank> loadRanks();

    Rank createRank(@NonNull final String name);

    void deleteRank(final int rankId);
}
