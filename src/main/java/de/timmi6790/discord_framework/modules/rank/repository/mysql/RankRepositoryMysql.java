package de.timmi6790.discord_framework.modules.rank.repository.mysql;

import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.rank.repository.RankRepository;
import de.timmi6790.discord_framework.modules.rank.repository.mysql.mappers.RankDatabaseMapper;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RankRepositoryMysql implements RankRepository {
    private static final String DATABASE_ID = "databaseId";
    private static final String RANK_ID = "rankId";

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

    /**
     * The Database.
     */
    private final Jdbi database;

    /**
     * Instantiates a new Rank repository mysql.
     *
     * @param rankModule        the rank module
     * @param databaseModule    the database module
     * @param userDbModule      the user db module
     * @param permissionsModule the permissions module
     */
    public RankRepositoryMysql(final RankModule rankModule,
                               final DatabaseModule databaseModule,
                               final UserDbModule userDbModule,
                               final PermissionsModule permissionsModule) {
        this.database = databaseModule.getJdbi();
        this.database.registerRowMapper(
                new RankDatabaseMapper(
                        rankModule,
                        userDbModule,
                        permissionsModule
                )
        );
    }

    @Override
    public List<Rank> getRanks() {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_ALL_RANKS)
                        .mapTo(Rank.class)
                        .list()
        );
    }

    @Override
    public Rank createRank(@NonNull final String rankName) {
        return this.database.withHandle(handle -> {
            handle.createUpdate(INSERT_RANK)
                    .bind("rankName", rankName)
                    .execute();

            final int rankId = handle.createQuery(GET_LAST_INSERT_ID)
                    .mapTo(int.class)
                    .first();

            return this.getRank(rankId);
        });
    }

    @Override
    public Rank getRank(final int rankId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_RANK_BY_ID)
                        .bind(DATABASE_ID, rankId)
                        .mapTo(Rank.class)
                        .first()
        );
    }

    @Override
    public void deleteRank(final int rankId) {
        this.database.useHandle(handle -> {
            handle.createUpdate(SET_PLAYERS_PRIMARY_RANK_TO_DEFAULT_ON_RANK_DELETE)
                    .bind(DATABASE_ID, rankId)
                    .execute();
            handle.createUpdate(DELETE_RANK)
                    .bind(DATABASE_ID, rankId)
                    .execute();
        });
    }

    @Override
    public void addPermission(final int rankId, final int permissionId) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_RANK_PERMISSION)
                        .bind(RANK_ID, rankId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
    }

    @Override
    public void removePermission(final int rankId, final int permissionId) {
        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_RANK_PERMISSION)
                        .bind(RANK_ID, rankId)
                        .bind("permissionId", permissionId)
                        .execute()
        );
    }

    @Override
    public void addExtendedRank(final int rankId, final int extendedRankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_RANK_RELATION)
                        .bind(RANK_ID, rankId)
                        .bind("extendedRankId", extendedRankId)
                        .execute()
        );
    }

    @Override
    public void removeExtendedRank(final int rankId, final int extendedRankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_RANK_RELATION)
                        .bind(RANK_ID, rankId)
                        .bind("extendedRankId", extendedRankId)
                        .execute()
        );
    }

    @Override
    public void setRankName(final int rankId, final String newRankName) {
        this.database.useHandle(handle ->
                handle.createUpdate(SET_NAME)
                        .bind("newRankName", newRankName)
                        .bind(DATABASE_ID, rankId)
                        .execute()
        );
    }

    @Override
    public Set<Long> retrieveAllPlayerIdsForRank(final int rankId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_PLAYER_IDS_WITH_RANK)
                        .bind(DATABASE_ID, rankId)
                        .mapTo(long.class)
                        .collect(Collectors.toSet())
        );
    }
}
