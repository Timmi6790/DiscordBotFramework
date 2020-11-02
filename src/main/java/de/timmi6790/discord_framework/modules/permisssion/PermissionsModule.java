package de.timmi6790.discord_framework.modules.permisssion;


import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.repository.PermissionRepository;
import de.timmi6790.discord_framework.modules.permisssion.repository.PermissionRepositoryMysql;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores the permission nodes for all perms(player, group). With id and perm_node
 */
@EqualsAndHashCode(callSuper = true)
public class PermissionsModule extends AbstractModule {
    private final Map<Integer, String> permissionsMap = new HashMap<>();

    private PermissionRepository permissionRepository;

    public PermissionsModule() {
        super("Permissions");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.permissionRepository = new PermissionRepositoryMysql(this);
    }

    private int getPermissionIdOrInsert(@NonNull final String permissionNode) {
        return this.permissionRepository.retrievePermissionId(permissionNode)
                .orElseGet(() -> this.permissionRepository.insertPermission(permissionNode));
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
        for (final Map.Entry<Integer, String> entry : this.permissionsMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(permissionNode)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    public Optional<String> getPermissionFromId(final int id) {
        return Optional.ofNullable(this.permissionsMap.get(id));
    }
}
