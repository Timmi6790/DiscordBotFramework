package de.timmi6790.discord_framework.modules.permisssion.repository;

import lombok.NonNull;

import java.util.Optional;

/**
 * Permission repository.
 */
public interface PermissionRepository {
    /**
     * Tries to retrieve the id of the permission node from the repository.
     *
     * @param permissionNode the permission node
     * @return the permission id
     */
    Optional<Integer> retrievePermissionId(final @NonNull String permissionNode);

    /**
     * Inserts a new permission node into the repository.
     *
     * @param permissionNode the permission node
     * @return the permission id
     */
    int insertPermission(final @NonNull String permissionNode);
}
