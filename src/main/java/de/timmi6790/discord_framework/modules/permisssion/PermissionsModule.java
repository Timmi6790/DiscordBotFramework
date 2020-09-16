package de.timmi6790.discord_framework.modules.permisssion;


import de.timmi6790.discord_framework.datatypes.ConcurrentTwoLaneMap;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

/**
 * Stores the permission nodes for all perms(player, group).
 * With id and perm_node
 */
@EqualsAndHashCode(callSuper = true)
public class PermissionsModule extends AbstractModule {
    private static final String GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID();";
    private static final String GET_PERMISSION_ID = "SELECT id " +
            "FROM `permission` " +
            "WHERE permission.permission_node = :permNode " +
            "LIMIT 1;";
    private static final String INSERT_PERMISSION = "INSERT INTO permission(permission_node, default_permission) VALUES(:permNode, 0);";

    private final ConcurrentTwoLaneMap<Integer, String> permissionsMap = new ConcurrentTwoLaneMap<>();

    private Jdbi database;

    public PermissionsModule() {
        super("Permissions");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.database = this.getModuleOrThrow(DatabaseModule.class).getJdbi();
    }

    private Optional<Integer> getDatabasePermissionId(final @NonNull String permissionNode) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_PERMISSION_ID)
                        .bind("permNode", permissionNode)
                        .mapTo(int.class)
                        .findFirst()
        );
    }

    private int insertPermissionIntoDatabase(final @NonNull String permissionNode) {
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

    private int getPermissionIdOrInsert(@NonNull final String permissionNode) {
        return this.getDatabasePermissionId(permissionNode).orElseGet(() -> this.insertPermissionIntoDatabase(permissionNode));
    }

    public int addPermission(final @NonNull String permissionNode) {
        final Optional<Integer> permissionIdOpt = this.getPermissionId(permissionNode);
        if (permissionIdOpt.isPresent()) {
            return permissionIdOpt.get();
        }

        final int permissionIdDb = this.getPermissionIdOrInsert(permissionNode);
        this.permissionsMap.put(permissionIdDb, permissionNode);
        return permissionIdDb;
    }

    public boolean hasPermission(final @NonNull String permissionNode) {
        return this.permissionsMap.containsValue(permissionNode);
    }

    public boolean hasPermission(final int id) {
        return this.permissionsMap.containsKey(id);
    }

    public Optional<Integer> getPermissionId(@NonNull final String permissionNode) {
        return this.permissionsMap.getKeyOptional(permissionNode);
    }

    public Optional<String> getPermissionFromId(final int id) {
        return this.permissionsMap.getValueOptional(id);
    }
}
