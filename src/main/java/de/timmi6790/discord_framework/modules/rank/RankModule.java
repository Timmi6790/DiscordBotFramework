package de.timmi6790.discord_framework.modules.rank;


import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.rank.repository.RankRepository;
import de.timmi6790.discord_framework.modules.rank.repository.mysql.RankRepositoryMysql;
import lombok.*;

import java.util.*;

/**
 * Rank module.
 */
@EqualsAndHashCode
@ToString
public class RankModule implements Module {
    /**
     * Rank id to rank mapping
     */
    private final Map<Integer, Rank> rankMap = new HashMap<>();

    /**
     * The Rank repository.
     */
    @Getter(AccessLevel.PROTECTED)
    private final RankRepository rankRepository;

    /**
     * Instantiates a new Rank module.
     */
    public RankModule(final RankRepositoryMysql rankRepository) {
        this.rankRepository = rankRepository;
        this.loadRanksFromRepository();
    }

    @Override
    public String getName() {
        return "Rank";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
    }

    /**
     * Load all ranks from repository.
     */
    private void loadRanksFromRepository() {
        for (final Rank rank : this.rankRepository.getRanks()) {
            this.addRank(rank);
        }
    }

    /**
     * Add the rank to all maps and reload the permission cache of all ranks
     *
     * @param rank the rank
     */
    private void addRank(@NonNull final Rank rank) {
        this.rankMap.put(rank.getRepositoryId(), rank);
        this.invalidateAllPermCaches();
    }

    /**
     * Invalidate the rank permission cache for all ranks. This is required after specific actions, like rank deletions,
     * permission modifications, etc... .
     */
    public void invalidateAllPermCaches() {
        for (final Rank rank : this.rankMap.values()) {
            rank.invalidateCachedPermissions();
        }
    }

    /**
     * Check if a rank with the given rank id exists.
     *
     * @param rankId the rankId
     * @return has rank with id
     */
    public boolean hasRank(final int rankId) {
        return this.rankMap.containsKey(rankId);
    }

    /**
     * Check if a rank with the given rank name exists.
     *
     * @param rankName the rankName
     * @return has rank with name
     */
    public boolean hasRank(@NonNull final String rankName) {
        return this.getRank(rankName).isPresent();
    }

    /**
     * Tries to get a rank with the given rank id.
     *
     * @param rankId the id
     * @return the rank
     */
    public Optional<Rank> getRank(final int rankId) {
        return Optional.ofNullable(this.rankMap.get(rankId));
    }

    /**
     * Tries to get a rank with the given rank name.
     *
     * @param rankName the rank name
     * @return the rank
     */
    public Optional<Rank> getRank(@NonNull final String rankName) {
        synchronized (this.rankMap) {
            for (final Rank rank : this.rankMap.values()) {
                if (rank.getRankName().equalsIgnoreCase(rankName)) {
                    return Optional.of(rank);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Get all ranks
     *
     * @return the ranks
     */
    public Set<Rank> getRanks() {
        return new HashSet<>(this.rankMap.values());
    }

    /**
     * Create a new rank with the given rankName. All rank names are case insensitive.
     *
     * @param rankName the rank name
     * @return false if a rank already exists with the given name and true on success
     */
    public boolean createRank(@NonNull final String rankName) {
        if (this.hasRank(rankName)) {
            return false;
        }

        final Rank newRank = this.rankRepository.createRank(rankName);
        this.addRank(newRank);

        return true;
    }

    /**
     * Delete the rank. This is not possible with the default rank, repository id 1.
     *
     * @param rank the rank
     * @return the boolean
     */
    public boolean deleteRank(@NonNull final Rank rank) {
        return this.deleteRank(rank.getRepositoryId());
    }

    /**
     * Delete the rank. This is not possible with the default rank, repository id 1.
     *
     * @param rankId the rank id
     * @return the boolean
     */
    public boolean deleteRank(final int rankId) {
        // Never allow anyone to delete the default rank with the id 1
        if (!this.hasRank(rankId) || rankId == 1) {
            return false;
        }

        this.rankRepository.deleteRank(rankId);
        this.rankMap.remove(rankId);

        this.invalidateAllPermCaches();

        return true;
    }
}
