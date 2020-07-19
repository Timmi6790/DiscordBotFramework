package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.datatypes.ConcurrentTwoLaneMap;
import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RankManager {
    private static final RankMapper rankMapper = new RankMapper();

    private static final String GET_ALL_RANKS = "SELECT rank.id, rank_name rankName, GROUP_CONCAT(DISTINCT rank_permission.permission_id) permissions, GROUP_CONCAT(DISTINCT rank_relation.parent_rank_id) parentRanks " +
            "FROM rank " +
            "LEFT JOIN rank_permission ON rank_permission.rank_id = rank.id " +
            "LEFT JOIN rank_relation ON rank_relation.child_rank_id = rank.id " +
            "GROUP BY rank.id;";

    private static final String DELETE_RANK = "DELETE FROM rank WHERE rank.id = :databaseId LIMIT 1;";
    private static final String SET_PLAYERS_PRIMARY_RANK_TO_DEFAULT_ON_RANK_DELETE = "UPDATE player SET player.primary_rank = 1 WHERE player.primary_rank = :databaseId;";

    private static final String INSERT_RANK = "INSERT INTO `rank`(rank_name) VALUES(:rankName);";
    private static final String GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID();";
    private static final String GET_RANK_BY_ID = "SELECT rank.id, rank_name rankName, GROUP_CONCAT(DISTINCT rank_permission.permission_id) permissions, GROUP_CONCAT(DISTINCT rank_relation.parent_rank_id) parentRanks " +
            "FROM rank " +
            "LEFT JOIN rank_permission ON rank_permission.rank_id = rank.id " +
            "LEFT JOIN rank_relation ON rank_relation.child_rank_id = rank.id " +
            "WHERE rank.id = :databaseId " +
            "GROUP BY rank.id;";

    private final ConcurrentHashMap<Integer, Rank> rankMap = new ConcurrentHashMap<>();
    private final ConcurrentTwoLaneMap<Integer, String> rankMappingMap = new ConcurrentTwoLaneMap<>();

    public void loadRanksFromDatabase() {
        final List<Rank> rankList = DiscordBot.getDatabase().withHandle(handle ->
                handle.createQuery(GET_ALL_RANKS)
                        .map(rankMapper)
                        .list()
        );

        rankList.forEach(this::addRank);
    }

    public void invalidateAllPermCaches() {
        this.rankMap.values().forEach(Rank::invalidateCachedPermissions);
    }

    public void addRank(final Rank rank) {
        this.rankMappingMap.put(rank.getDatabaseId(), rank.getName());
        this.rankMap.put(rank.getDatabaseId(), rank);
        this.invalidateAllPermCaches();
    }

    public boolean hasRank(final int id) {
        return this.rankMap.containsKey(id);
    }

    public boolean hasRank(final String name) {
        return this.rankMappingMap.containsValue(name);
    }

    public Optional<Rank> getRank(final int id) {
        return Optional.ofNullable(this.rankMap.get(id));
    }

    public Optional<Rank> getRank(final String name) {
        final Optional<Integer> rankId = this.rankMappingMap.getKeyOptional(name);
        if (!rankId.isPresent()) {
            return Optional.empty();
        }

        return this.getRank(rankId.get());
    }

    public Set<Rank> getRanks() {
        return new HashSet<>(this.rankMap.values());
    }

    public boolean createRank(final String name) {
        if (this.hasRank(name)) {
            return false;
        }

        final Rank newRank = DiscordBot.getDatabase().withHandle(handle -> {
            handle.createUpdate(INSERT_RANK)
                    .bind("rankName", name)
                    .execute();

            final int rankId = handle.createQuery(GET_LAST_INSERT_ID)
                    .mapTo(int.class)
                    .first();

            return handle.createQuery(GET_RANK_BY_ID).bind("databaseId", rankId)
                    .map(rankMapper)
                    .first();
        });
        this.addRank(newRank);

        return true;
    }

    public boolean deleteRank(final int rankId) {
        // Never allow anyone to delete the default rank with id 1
        if (!this.hasRank(rankId) || rankId == 1) {
            return false;
        }

        DiscordBot.getDatabase().useHandle(handle -> {
            handle.createUpdate(SET_PLAYERS_PRIMARY_RANK_TO_DEFAULT_ON_RANK_DELETE)
                    .bind("databaseId", rankId)
                    .execute();
            handle.createUpdate(DELETE_RANK)
                    .bind("databaseId", rankId)
                    .execute();
        });
        this.rankMap.remove(rankId);
        this.rankMappingMap.remove(rankId);

        this.invalidateAllPermCaches();

        return true;
    }

    public boolean deleteRank(@NonNull final Rank rank) {
        return this.deleteRank(rank.getDatabaseId());
    }
}
