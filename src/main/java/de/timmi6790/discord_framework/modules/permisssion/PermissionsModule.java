package de.timmi6790.discord_framework.modules.permisssion;


import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.repository.PermissionRepository;
import de.timmi6790.discord_framework.modules.permisssion.repository.mysql.PermissionRepositoryMysql;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores the permission nodes for all perms(player, group). With an id and perm_node
 */
@EqualsAndHashCode(callSuper = true)
public class PermissionsModule extends AbstractModule {
    private final Map<Integer, String> permissionsMap = new HashMap<>();

    private PermissionRepository permissionRepository;

    /**
     * Instantiates a new Permissions module.
     */
    public PermissionsModule() {
        super("Permissions");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.permissionRepository = new PermissionRepositoryMysql(
                this.getModuleOrThrow(DatabaseModule.class).getJdbi()
        );

        return true;
    }

    private int getPermissionIdOrInsert(@NonNull final String permissionNode) {
        return this.permissionRepository.retrievePermissionId(permissionNode)
                .orElseGet(() -> this.permissionRepository.insertPermission(permissionNode));
    }

    /**
     * Add a new permission node to the system. It will first try to find an already existing id inside the repository
     * and if not found it will create a new permission id inside the repository for the permission node.
     *
     * @param permissionNode the permission node
     * @return the permission id
     */
    public int addPermission(final @NonNull String permissionNode) {
        // Check if the permission node already exists
        final Optional<Integer> permissionIdOpt = this.getPermissionId(permissionNode);
        if (permissionIdOpt.isPresent()) {
            return permissionIdOpt.get();
        }

        // Insert the new permission node into the repository
        final int permissionIdDb = this.getPermissionIdOrInsert(permissionNode);
        this.permissionsMap.put(permissionIdDb, permissionNode);
        return permissionIdDb;
    }

    /**
     * Has permission node loaded. This does not include unloaded entries inside the repository.
     *
     * @param permissionNode the permission node
     * @return has permission node
     */
    public boolean hasPermission(final @NonNull String permissionNode) {
        return this.permissionsMap.containsValue(permissionNode);
    }

    /**
     * Has permission id loaded. This does not include unloaded entries inside the repository.
     *
     * @param permissionId the permission id
     * @return has permission id
     */
    public boolean hasPermission(final int permissionId) {
        return this.permissionsMap.containsKey(permissionId);
    }

    /**
     * Get the permission id from from the permission node. This does not include unloaded entries inside the
     * repository.
     *
     * @param permissionNode the permission node
     * @return the permission id
     */
    public Optional<Integer> getPermissionId(@NonNull final String permissionNode) {
        for (final Map.Entry<Integer, String> entry : this.permissionsMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(permissionNode)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    /**
     * Get the permission node from from the permission id. This does not include unloaded entries inside the
     * repository.
     *
     * @param permissionId the permission id
     * @return the permission node
     */
    public Optional<String> getPermissionFromId(final int permissionId) {
        return Optional.ofNullable(this.permissionsMap.get(permissionId));
    }
}
