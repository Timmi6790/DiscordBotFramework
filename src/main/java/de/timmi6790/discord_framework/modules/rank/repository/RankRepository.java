package de.timmi6790.discord_framework.modules.rank.repository;

import de.timmi6790.discord_framework.modules.rank.Rank;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

/**
 * Rank repository.
 */
public interface RankRepository {
    /**
     * Get all ranks inside the repository.
     *
     * @return all ranks
     */
    List<Rank> getRanks();

    /**
     * Create a new rank inside the repository.
     *
     * @param rankName the rankName
     * @return the rank
     */
    Rank createRank(@NonNull final String rankName);

    /**
     * Get a rank by id inside the repository.
     *
     * @param rankId the rank id
     * @return the rank
     */
    Rank getRank(int rankId);

    /**
     * Delete a rank from the repository.
     *
     * @param rankId the rank id
     */
    void deleteRank(final int rankId);

    /**
     * Add permissions to the rank.
     *
     * @param rankId       the rank id
     * @param permissionId the permission id
     */
    void addPermission(int rankId, final int permissionId);

    /**
     * Remove permissions from the rank.
     *
     * @param rankId       the rank id
     * @param permissionId the permission id
     */
    void removePermission(int rankId, final int permissionId);

    /**
     * Extend the rank with another rank.
     *
     * @param rankId         the rank id
     * @param extendedRankId the extended rank id
     */
    void addExtendedRank(int rankId, final int extendedRankId);

    /**
     * Remove the extended rank from a rank.
     *
     * @param rankId         the rank id
     * @param extendedRankId the extended rank id
     */
    void removeExtendedRank(int rankId, final int extendedRankId);

    /**
     * Rename the rank.
     *
     * @param rankId      the rank id
     * @param newRankName the newRankName
     */
    void setRankName(int rankId, final String newRankName);

    /**
     * Retrieve all player ids with the rank.
     *
     * @param rankId the rank id
     * @return all players with the rank
     */
    Set<Long> retrieveAllPlayerIdsForRank(int rankId);
}
