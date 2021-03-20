package de.timmi6790.discord_framework.modules.permisssion.repository.mysql;

import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.Service;
import de.timmi6790.discord_framework.modules.permisssion.repository.PermissionRepository;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

/**
 * Mysql implementation of the permission repository.
 */
@Service
public class PermissionRepositoryMysql implements PermissionRepository {
    private static final String GET_PERMISSION_ID = "SELECT id " +
            "FROM `permission` " +
            "WHERE permission.permission_node = :permNode " +
            "LIMIT 1;";
    private static final String INSERT_PERMISSION = "INSERT INTO permission(permission_node) VALUES(:permNode);";

    private final Jdbi database;

    /**
     * Instantiates a new Permission repository mysql.
     *
     * @param database the database
     */
    public PermissionRepositoryMysql(final DatabaseModule database) {
        this.database = database.getJdbi();
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
        return this.database.withHandle(handle ->
                handle.createUpdate(INSERT_PERMISSION)
                        .bind("permNode", permissionNode)
                        .executeAndReturnGeneratedKeys()
                        .mapTo(int.class)
                        .first()
        );
    }
}
