package de.timmi6790.discord_framework.module.modules.rank;

import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import lombok.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Rank instance.
 */
@Data
@Getter(AccessLevel.NONE)
@Setter(AccessLevel.NONE)
public class Rank {
    /**
     * The Rank module.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final RankModule rankModule;
    /**
     * The Permissions module.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final PermissionsModule permissionsModule;

    // Data
    /**
     * The repository id.
     */
    @Getter
    private final int repositoryId;
    /**
     * The permission of every extended rank is used inside the permission check of the rank.
     */
    @Getter
    private final Set<Integer> extendedRankIds;
    /**
     * The Permission ids of the rank.
     */
    private final Set<Integer> permissionIds;

    /**
     * The name of the rank.
     */
    @Getter
    private String rankName;
    /**
     * Calculating all permissions is expensive that is the reason after calculating it once it is cached. The cache is
     * invalidated after specific actions like, rank deleting, rank permission modifications.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Integer> cachedAllPermissions;

    /**
     * Instantiates a new Rank.
     *
     * @param rankModule        the rank module
     * @param permissionsModule the permissions module
     * @param repositoryId      the database id
     * @param rankName          the rank name
     * @param extendedRankIds   the extended rank ids
     * @param permissionIds     the permission ids
     */
    public Rank(final RankModule rankModule,
                final PermissionsModule permissionsModule,
                final int repositoryId,
                final String rankName,
                final Set<Integer> extendedRankIds,
                final Set<Integer> permissionIds) {
        this.rankModule = rankModule;
        this.permissionsModule = permissionsModule;
        this.repositoryId = repositoryId;
        this.rankName = rankName;
        this.extendedRankIds = extendedRankIds;
        this.permissionIds = permissionIds;
    }

    /**
     * Gets all permission ids. This is a heavy calculation because we need to scan all extended ranks
     *
     * @return all permission ids
     */
    private Set<Integer> getAllPermissionIds() {
        // Check if already calculated
        if (this.cachedAllPermissions != null) {
            return this.cachedAllPermissions;
        }

        final Set<Integer> foundPermissions = new HashSet<>(this.permissionIds);
        final Set<Integer> seen = new HashSet<>();
        final ArrayDeque<Integer> queue = new ArrayDeque<>(this.extendedRankIds);
        while (!queue.isEmpty()) {
            final Optional<Rank> rankOpt = this.rankModule.getRank(queue.pop());
            if (rankOpt.isPresent()) {
                final Rank rank = rankOpt.get();

                foundPermissions.addAll(rank.getPermissionIds(false));
                for (final int rankId : rank.getExtendedRankIds()) {
                    if (seen.add(rankId)) {
                        queue.add(rankId);
                    }
                }
            }
        }

        this.cachedAllPermissions = foundPermissions;
        return foundPermissions;
    }

    /**
     * Invalidate cached permissions.
     */
    public void invalidateCachedPermissions() {
        this.cachedAllPermissions = null;
    }

    /**
     * Gets extended ranks.
     *
     * @return the extended ranks
     */
    public Set<Rank> getExtendedRanks() {
        final Set<Rank> ranks = new HashSet<>();
        for (final int rankId : this.extendedRankIds) {
            this.rankModule.getRank(rankId).ifPresent(ranks::add);
        }
        return ranks;
    }

    // Permissions

    /**
     * Check if the rank contains the permission id.
     *
     * @param permissionId          permission id
     * @param includedExtendedRanks included extended ranks
     * @return has permission
     */
    public boolean hasPermission(final int permissionId, final boolean includedExtendedRanks) {
        return this.getPermissionIds(includedExtendedRanks).contains(permissionId);
    }

    /**
     * Repository help method. DON'T USE THIS OUTSIDE OF THE REPOSITORY!
     *
     * @param permissionId the permission id
     */
    public void addPermissionRepositoryOnly(final int permissionId) {
        this.permissionIds.add(permissionId);
    }

    /**
     * Tries to add the permission id to the rank.
     *
     * @param permissionId the permission id
     * @return if permission was added to the rank
     */
    public boolean addPermission(final int permissionId) {
        if (this.hasPermission(permissionId, false)) {
            return false;
        }

        this.rankModule.getRankRepository().addPermission(this.getRepositoryId(), permissionId);
        this.permissionIds.add(permissionId);
        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    /**
     * Tries to remove the permission id from the rank.
     *
     * @param permissionId the permission id
     * @return if permission was removed from the rank
     */
    public boolean removePermission(final int permissionId) {
        if (!this.hasPermission(permissionId, false)) {
            return false;
        }

        this.rankModule.getRankRepository().removePermission(this.getRepositoryId(), permissionId);
        this.permissionIds.remove(permissionId);
        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    /**
     * Get all permission nodes of the rank
     *
     * @param includeExtendedRanks include extended ranks
     * @return the permission nodes
     */
    public Set<String> getPermissions(final boolean includeExtendedRanks) {
        final Set<String> permissions = new HashSet<>();
        for (final int permissionId : this.getPermissionIds(includeExtendedRanks)) {
            this.permissionsModule
                    .getPermissionFromId(permissionId)
                    .ifPresent(permissions::add);
        }
        return permissions;
    }

    /**
     * Gte all permission ids of the rank
     *
     * @param includeExtendedRanks include extended ranks
     * @return the permission ids
     */
    public Set<Integer> getPermissionIds(final boolean includeExtendedRanks) {
        if (includeExtendedRanks) {
            return this.getAllPermissionIds();
        }
        return this.permissionIds;
    }

    // Extended Ranks

    /**
     * Check if the rank extends the rank id.
     *
     * @param rankId the rank id
     * @return does extend
     */
    public boolean hasExtendedRank(final int rankId) {
        return this.getExtendedRankIds().contains(rankId);
    }

    /**
     * Check if the rank extends the rank.
     *
     * @param rank the rank
     * @return does extend
     */
    public boolean hasExtendedRank(@NonNull final Rank rank) {
        return this.hasExtendedRank(rank.getRepositoryId());
    }

    /**
     * Tries to add the rank as an extended rank. This will fail if it is the same rank or we already extend on it.
     *
     * @param rank the rank
     * @return if the rank was added as an extended rank
     */
    public boolean addExtendedRank(@NonNull final Rank rank) {
        return this.addExtendedRank(rank.getRepositoryId());
    }

    /**
     * Repository help method. DON'T USE THIS OUTSIDE OF THE REPOSITORY!
     *
     * @param rankId the rank id
     */
    public void addExtendedRankRepositoryOnly(final int rankId) {
        this.extendedRankIds.add(rankId);
    }

    /**
     * Tries to add the rankId as an extended rank. This will fail if it is the same rank or we already extend on it.
     *
     * @param rankId the rank id
     * @return if the rank was added as an extended rank
     */
    public boolean addExtendedRank(final int rankId) {
        if (rankId == this.getRepositoryId() || this.hasExtendedRank(rankId)) {
            return false;
        }

        this.rankModule.getRankRepository().addExtendedRank(this.getRepositoryId(), rankId);
        this.extendedRankIds.add(rankId);

        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    /**
     * Tries to remove the rank from the extended ranks
     *
     * @param rank the rank
     * @return if the rank was removed from the extended rank
     */
    public boolean removeExtendedRank(@NonNull final Rank rank) {
        return this.removeExtendedRank(rank.getRepositoryId());
    }

    /**
     * Tries to remove the rank from the extended ranks
     *
     * @param rankId the rank id
     * @return if the rank was removed from the extended rank
     */
    public boolean removeExtendedRank(final int rankId) {
        if (!this.hasExtendedRank(rankId)) {
            return false;
        }

        this.rankModule.getRankRepository().removeExtendedRank(this.getRepositoryId(), rankId);
        this.getExtendedRankIds().remove(rankId);

        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    // Name

    /**
     * Sets a new rank name
     *
     * @param newRankName the new rank name
     * @return if the rank was changed
     */
    public boolean setRankName(final String newRankName) {
        if (this.rankName.equalsIgnoreCase(newRankName)) {
            return false;
        }

        this.rankModule.getRankRepository().setRankName(this.getRepositoryId(), newRankName);
        this.rankName = newRankName;
        return true;
    }
}
