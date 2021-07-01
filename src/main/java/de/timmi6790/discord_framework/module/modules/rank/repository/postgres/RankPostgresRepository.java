package de.timmi6790.discord_framework.module.modules.rank.repository.postgres;

import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.rank.repository.RankRepository;
import de.timmi6790.discord_framework.module.modules.rank.repository.postgres.mappers.RankMapper;
import de.timmi6790.discord_framework.module.modules.rank.repository.postgres.reducers.RankReducer;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.stream.Collectors;

public class RankPostgresRepository implements RankRepository {
    private static final String DATABASE_ID = "databaseId";
    private static final String RANK_ID = "rankId";

    private static final String GET_ALL_RANKS = "SELECT rank.id, rank_name rankName, rank_permission.permission_id, rank_relation.parent_rank_id " +
            "FROM rank.ranks rank " +
            "LEFT JOIN rank.rank_permissions rank_permission ON rank_permission.rank_id = rank.id " +
            "LEFT JOIN rank.rank_relations rank_relation ON rank_relation.child_rank_id = rank.id;";

    private static final String DELETE_RANK = "DELETE FROM rank.ranks WHERE id = :databaseId;";

    private static final String INSERT_RANK = "INSERT INTO rank.ranks(rank_name) VALUES(:rankName) RETURNING id;";

    private static final String GET_RANK_BY_ID = "SELECT rank.id, rank_name rankName, rank_permission.permission_id, rank_relation.parent_rank_id " +
            "FROM rank.ranks rank " +
            "LEFT JOIN rank.rank_permissions rank_permission ON rank_permission.rank_id = rank.id " +
            "LEFT JOIN rank.rank_relations rank_relation ON rank_relation.child_rank_id = rank.id " +
            "WHERE rank.id = :databaseId;";


    private static final String INSERT_RANK_PERMISSION = "INSERT INTO rank.rank_permissions(rank_id, permission_id) VALUES(:rankId, :permissionId);";
    private static final String DELETE_RANK_PERMISSION = "DELETE FROM rank.rank_permissions WHERE rank_id = :rankId AND permission_id = :permissionId";

    private static final String INSERT_RANK_RELATION = "INSERT INTO rank.rank_relations(parent_rank_id, child_rank_id) VALUES(:extendedRankId, :rankId);";
    private static final String DELETE_RANK_RELATION = "DELETE FROM rank.rank_relations WHERE parent_rank_id = :extendedRankId AND child_rank_id = :rankId;";

    private static final String SET_NAME = "UPDATE rank.ranks SET rank_name = :newRankName WHERE id = :databaseId;";

    /**
     * The Database.
     */
    private final Jdbi database;

    /**
     * Instantiates a new Rank postgres repository.
     *
     * @param rankModule        the rank module
     * @param databaseModule    the database module
     * @param permissionsModule the permissions module
     */
    public RankPostgresRepository(final RankModule rankModule,
                                  final DatabaseModule databaseModule,
                                  final PermissionsModule permissionsModule) {
        this.database = databaseModule.getJdbi();
        this.database.registerRowMapper(
                new RankMapper(
                        rankModule,
                        permissionsModule
                )
        );
    }

    @Override
    public List<Rank> getRanks() {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_ALL_RANKS)
                        .reduceRows(new RankReducer())
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Rank createRank(@NonNull final String rankName) {
        final int rankId = this.database.withHandle(handle ->
                handle.createQuery(INSERT_RANK)
                        .bind("rankName", rankName)
                        .mapTo(int.class)
                        .first()
        );
        return this.getRank(rankId);
    }

    @Override
    public Rank getRank(final int rankId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_RANK_BY_ID)
                        .bind(DATABASE_ID, rankId)
                        .reduceRows(new RankReducer())
                        .findAny()
                        .orElseThrow(RuntimeException::new)
        );
    }

    @Override
    public void deleteRank(final int rankId) {
        this.database.useHandle(handle ->
                handle.createUpdate(DELETE_RANK)
                        .bind(DATABASE_ID, rankId)
                        .execute()
        );
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
}
