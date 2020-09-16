package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class Rank {
    private static final String RANK_ID = "rankId";
    private static final String DATABASE_ID = "databaseId";

    private static final String INSERT_RANK_PERMISSION = "INSERT INTO rank_permission(rank_id, permission_id) VALUES(:rankId, :permissionId);";
    private static final String DELETE_RANK_PERMISSION = "DELETE FROM rank_permission WHERE rank_id = :rankId AND permission_id = :permissionId LIMIT 1";

    private static final String INSERT_RANK_RELATION = "INSERT INTO rank_relation(parent_rank_id, child_rank_id) VALUES(:extendedRankId, :rankId);";
    private static final String DELETE_RANK_RELATION = "DELETE FROM rank_relation WHERE parent_rank_id = :extendedRankId AND child_rank_id = :rankId LIMIT 1;";

    private static final String SET_NAME = "UPDATE rank SET rank.rank_name = :newRankName WHERE rank.id = :databaseId LIMIT 1;";

    private static final String GET_PLAYER_IDS_WITH_RANK = "SELECT discordId " +
            "FROM player " +
            "LEFT JOIN player_rank ON player_rank.player_id = player.id " +
            "WHERE player.primary_rank = :databaseId " +
            "OR player_rank.rank_id = :databaseId " +
            "GROUP BY player.id;";

    private final int databaseId;
    private final Set<Integer> extendedRanks;
    private final Set<Integer> permissions;
    private String name;
    private Set<Integer> cachedAllPermissions;

    private final Jdbi database;
    private final RankModule rankModule;
    private final UserDbModule userDbModule;

    public Rank(final Jdbi database, final RankModule rankModule, final UserDbModule userDbModule, final int databaseId, final String name,
                final Set<Integer> extendedRanks, final Set<Integer> permissions) {
        this.database = database;
        this.rankModule = rankModule;
        this.userDbModule = userDbModule;
        this.databaseId = databaseId;
        this.name = name;
        this.extendedRanks = extendedRanks;
        this.permissions = permissions;
    }

    public void invalidateCachedPermissions() {
        this.cachedAllPermissions = null;
    }

    // Permissions
    public boolean hasPermission(final int permissionId) {
        return this.permissions.contains(permissionId);
    }

    public boolean addPermission(final int permissionId) {
        if (this.hasPermission(permissionId)) {
            return false;
        }

        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_RANK_PERMISSION)
                        .bind(RANK_ID, this.databaseId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
        this.permissions.add(permissionId);

        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    public boolean removePermission(final int permissionId) {
        if (!this.hasPermission(permissionId)) {
            return false;
        }

        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_RANK_PERMISSION)
                        .bind(RANK_ID, this.databaseId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
        this.permissions.remove(permissionId);

        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    public Set<Integer> getAllPermissions() {
        if (this.cachedAllPermissions != null) {
            return this.cachedAllPermissions;
        }

        final Set<Integer> foundPermissions = new HashSet<>(this.permissions);
        final Set<Integer> seen = new HashSet<>();
        final ArrayDeque<Integer> queue = new ArrayDeque<>(this.extendedRanks);

        while (!queue.isEmpty()) {
            this.rankModule.getRank(queue.pop()).ifPresent(rank -> {
                foundPermissions.addAll(rank.getPermissions());

                rank.getExtendedRanks()
                        .stream()
                        .filter(extendId -> !seen.contains(extendId))
                        .forEach(extendId -> {
                            seen.add(extendId);
                            queue.add(extendId);
                        });
            });
        }

        this.cachedAllPermissions = foundPermissions;
        return foundPermissions;
    }

    // Extended Ranks
    public boolean hasExtendedRank(final int rankId) {
        return this.extendedRanks.contains(rankId);
    }

    public boolean hasExtendedRank(@NonNull final Rank rank) {
        return this.hasExtendedRank(rank.getDatabaseId());
    }

    public boolean addExtendedRank(final int rankId) {
        if (rankId == this.databaseId || this.extendedRanks.contains(rankId)) {
            return false;
        }

        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_RANK_RELATION)
                        .bind(RANK_ID, this.databaseId)
                        .bind("extendedRankId", rankId)
                        .execute()
        );
        this.extendedRanks.add(rankId);

        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    public boolean addExtendedRank(@NonNull final Rank rank) {
        return this.addExtendedRank(rank.getDatabaseId());
    }

    public boolean removeExtendedRank(final int rankId) {
        if (!this.extendedRanks.contains(rankId)) {
            return false;
        }

        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_RANK_RELATION)
                        .bind(RANK_ID, this.databaseId)
                        .bind("extendedRankId", rankId)
                        .execute()
        );
        this.extendedRanks.remove(rankId);

        this.rankModule.invalidateAllPermCaches();

        return true;
    }

    public boolean removeExtendedRank(@NonNull final Rank rank) {
        return this.removeExtendedRank(rank.getDatabaseId());
    }

    // Name
    public void setName(final String name) {
        this.database.useHandle(handle ->
                handle.createUpdate(SET_NAME)
                        .bind("newRankName", name)
                        .bind(DATABASE_ID, this.databaseId)
                        .execute()
        );
        this.name = name;
    }

    // Players
    public long retrievePlayerCount() {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_PLAYER_IDS_WITH_RANK)
                        .bind(DATABASE_ID, this.databaseId)
                        .mapTo(long.class)
                        .list()

        ).size();
    }

    public Set<UserDb> retrieveAllPlayers() {
        // I think it is the best solution to just get the discord ids
        final List<Long> playerIds = this.database.withHandle(handle ->
                handle.createQuery(GET_PLAYER_IDS_WITH_RANK)
                        .bind(DATABASE_ID, this.databaseId)
                        .mapTo(long.class)
                        .list()
        );

        return playerIds.parallelStream()
                .map(this.userDbModule::getOrCreate)
                .collect(Collectors.toSet());
    }
}
