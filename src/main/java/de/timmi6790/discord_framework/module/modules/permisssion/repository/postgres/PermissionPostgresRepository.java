package de.timmi6790.discord_framework.module.modules.permisssion.repository.postgres;

import de.timmi6790.discord_framework.module.modules.permisssion.repository.PermissionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

/**
 * Postgres implementation of the permission repository.
 */
@RequiredArgsConstructor
public class PermissionPostgresRepository implements PermissionRepository {
    private static final String GET_PERMISSION_ID = "SELECT id " +
            "FROM permissions permission " +
            "WHERE permission.permission_node = :permNode " +
            "LIMIT 1;";
    private static final String INSERT_PERMISSION = "INSERT INTO permissions(permission_node) VALUES(:permNode) RETURNING id;";

    private final Jdbi database;

    @Override
    public Optional<Integer> retrievePermissionId(@NonNull final String permissionNode) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_PERMISSION_ID)
                        .bind("permNode", permissionNode)
                        .mapTo(int.class)
                        .findFirst()
        );
    }

    @Override
    public int insertPermission(@NonNull final String permissionNode) {
        return this.database.withHandle(handle ->
                handle.createQuery(INSERT_PERMISSION)
                        .bind("permNode", permissionNode)
                        .mapTo(int.class)
                        .first()
        );
    }
}
