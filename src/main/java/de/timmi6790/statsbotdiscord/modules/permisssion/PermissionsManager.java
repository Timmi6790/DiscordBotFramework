package de.timmi6790.statsbotdiscord.modules.permisssion;


import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.datatypes.ConcurrentTwoLaneMap;
import lombok.NonNull;

import java.util.Optional;

/**
 * Stores the permission nodes for all perms(player, group).
 * With id and perm_node
 */
public class PermissionsManager {
    private static final String GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID();";
    private static final String GET_PERMISSION_ID = "SELECT id " +
            "FROM `permission` " +
            "WHERE permission.permission_node = :permNode " +
            "LIMIT 1;";
    private static final String INSERT_PERMISSION = "INSERT INTO permission(permission_node, default_permission) VALUES(:permNode, 0);";

    private final ConcurrentTwoLaneMap<Integer, String> permissionsMap = new ConcurrentTwoLaneMap<>();

    private Optional<Integer> getDatabasePermissionId(final @NonNull String permission) {
        return StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery(GET_PERMISSION_ID)
                        .bind("permNode", permission)
                        .mapTo(int.class)
                        .findFirst()
        );
    }

    private int insertPermissionIntoDatabase(final @NonNull String permission) {
        return StatsBot.getDatabase().withHandle(handle -> {
                    handle.createUpdate(INSERT_PERMISSION)
                            .bind("permNode", permission)
                            .execute();

                    return handle.createQuery(GET_LAST_INSERT_ID)
                            .mapTo(int.class)
                            .first();
                }
        );
    }

    public int addPermission(final @NonNull String permission) {
        final Integer permissionId = this.permissionsMap.getKey(permission);
        if (permissionId != null) {
            return permissionId;
        }

        final Integer permissionIdDb = this.getDatabasePermissionId(permission)
                .orElseGet(() -> this.insertPermissionIntoDatabase(permission));

        this.permissionsMap.put(permissionIdDb, permission);
        return permissionIdDb;
    }

    public boolean hasPermission(final @NonNull String permission) {
        return this.permissionsMap.containsValue(permission);
    }

    public boolean hasPermission(final int id) {
        return this.permissionsMap.containsKey(id);
    }

    public Optional<Integer> getPermissionId(@NonNull final String permission) {
        return this.permissionsMap.getKeyOptional(permission);
    }

    public Optional<String> getPermissionFromId(final int id) {
        return this.permissionsMap.getValueOptional(id);
    }
}
