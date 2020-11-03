package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(exclude = {"rankModule", "userDbModule", "permissionsModule"})
public class Rank {
    private final int databaseId;
    private final Set<Integer> extendedRankIds;
    private final Set<Integer> permissionIds;
    private final RankModule rankModule;
    private final UserDbModule userDbModule;
    private final PermissionsModule permissionsModule;
    private String name;
    private Set<Integer> cachedAllPermissions;

    public Rank(final RankModule rankModule,
                final UserDbModule userDbModule,
                final PermissionsModule permissionsModule,
                final int databaseId,
                final String name,
                final Set<Integer> extendedRankIds,
                final Set<Integer> permissionIds) {
        this.rankModule = rankModule;
        this.userDbModule = userDbModule;
        this.permissionsModule = permissionsModule;
        this.databaseId = databaseId;
        this.name = name;
        this.extendedRankIds = extendedRankIds;
        this.permissionIds = permissionIds;
    }

    public void invalidateCachedPermissions() {
        this.setCachedAllPermissions(null);
    }

    public Set<Rank> getExtendedRanks() {
        final Set<Rank> ranks = new HashSet<>();
        for (final int rankId : this.getExtendedRankIds()) {
            this.getRankModule().getRank(rankId).ifPresent(ranks::add);
        }
        return ranks;
    }

    // Permissions
    public boolean hasPermission(final int permissionId) {
        return this.getPermissionIds().contains(permissionId);
    }

    public boolean addPermission(final int permissionId) {
        if (this.hasPermission(permissionId)) {
            return false;
        }


        this.getRankModule().getRankRepository().addPermission(this.getDatabaseId(), permissionId);
        this.getPermissionIds().add(permissionId);

        this.getRankModule().invalidateAllPermCaches();

        return true;
    }

    public boolean removePermission(final int permissionId) {
        if (!this.hasPermission(permissionId)) {
            return false;
        }


        this.getRankModule().getRankRepository().removePermission(this.getDatabaseId(), permissionId);
        this.getPermissionIds().remove(permissionId);

        this.getRankModule().invalidateAllPermCaches();

        return true;
    }

    public Set<String> getPermissions() {
        final Set<String> permissions = new HashSet<>();
        for (final int id : this.getPermissionIds()) {
            this.getPermissionsModule().getPermissionFromId(id).ifPresent(permissions::add);
        }
        return permissions;
    }

    public Set<String> getAllPermissions() {
        final Set<String> permissions = new HashSet<>();
        for (final int id : this.getAllPermissionIds()) {
            this.getPermissionsModule().getPermissionFromId(id).ifPresent(permissions::add);
        }
        return permissions;
    }

    public Set<Integer> getAllPermissionIds() {
        if (this.getCachedAllPermissions() != null) {
            return this.getCachedAllPermissions();
        }

        final Set<Integer> foundPermissions = new HashSet<>(this.getPermissionIds());
        final Set<Integer> seen = new HashSet<>();
        final ArrayDeque<Integer> queue = new ArrayDeque<>(this.getExtendedRankIds());

        while (!queue.isEmpty()) {
            this.getRankModule().getRank(queue.pop()).ifPresent(rank -> {
                foundPermissions.addAll(rank.getPermissionIds());

                rank.getExtendedRankIds()
                        .stream()
                        .filter(extendId -> !seen.contains(extendId))
                        .forEach(extendId -> {
                            seen.add(extendId);
                            queue.add(extendId);
                        });
            });
        }

        this.setCachedAllPermissions(foundPermissions);
        return foundPermissions;
    }

    // Extended Ranks
    public boolean hasExtendedRank(final int rankId) {
        return this.getExtendedRankIds().contains(rankId);
    }

    public boolean hasExtendedRank(@NonNull final Rank rank) {
        return this.hasExtendedRank(rank.getDatabaseId());
    }

    public boolean addExtendedRank(final int rankId) {
        if (rankId == this.getDatabaseId() || this.hasExtendedRank(rankId)) {
            return false;
        }

        this.getRankModule().getRankRepository().addExtendedRank(this.getDatabaseId(), rankId);
        this.getExtendedRankIds().add(rankId);

        this.getRankModule().invalidateAllPermCaches();

        return true;
    }

    public boolean addExtendedRank(@NonNull final Rank rank) {
        return this.addExtendedRank(rank.getDatabaseId());
    }

    public boolean removeExtendedRank(final int rankId) {
        if (!this.hasExtendedRank(rankId)) {
            return false;
        }

        this.getRankModule().getRankRepository().removeExtendedRank(this.getDatabaseId(), rankId);
        this.getExtendedRankIds().remove(rankId);

        this.getRankModule().invalidateAllPermCaches();

        return true;
    }

    public boolean removeExtendedRank(@NonNull final Rank rank) {
        return this.removeExtendedRank(rank.getDatabaseId());
    }

    // Name
    public void setName(final String name) {
        this.getRankModule().getRankRepository().setRankName(this.getDatabaseId(), name);
        this.name = name;
    }

    // Players
    public long retrievePlayerCount() {
        return this.getRankModule().getRankRepository().retrieveAllPlayerIdsForRank(this.getDatabaseId()).size();
    }

    public Set<UserDb> retrieveAllPlayers() {
        // I think it is the best solution to just get the discord ids
        final Set<Long> playerIds = this.getRankModule().getRankRepository().retrieveAllPlayerIdsForRank(this.getDatabaseId());
        return playerIds.parallelStream()
                .map(this.getUserDbModule()::getOrCreate)
                .collect(Collectors.toSet());
    }
}
