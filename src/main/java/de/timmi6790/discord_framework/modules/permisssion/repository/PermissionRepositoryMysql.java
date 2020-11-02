package de.timmi6790.discord_framework.modules.permisssion.repository;

import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class PermissionRepositoryMysql implements PermissionRepository {
    private static final String GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID();";
    private static final String GET_PERMISSION_ID = "SELECT id " +
            "FROM `permission` " +
            "WHERE permission.permission_node = :permNode " +
            "LIMIT 1;";
    private static final String INSERT_PERMISSION = "INSERT INTO permission(permission_node, default_permission) VALUES(:permNode, 0);";

    private final Jdbi database;

    public PermissionRepositoryMysql(final PermissionsModule module) {
        this.database = module.getModuleOrThrow(DatabaseModule.class).getJdbi();
    }

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
        return this.database.withHandle(handle -> {
                    handle.createUpdate(INSERT_PERMISSION)
                            .bind("permNode", permissionNode)
                            .execute();

                    return handle.createQuery(GET_LAST_INSERT_ID)
                            .mapTo(int.class)
                            .first();
                }
        );
    }
}
