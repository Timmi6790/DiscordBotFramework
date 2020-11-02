package de.timmi6790.discord_framework.modules.permisssion.repository;

import lombok.NonNull;

import java.util.Optional;

public interface PermissionRepository {
    Optional<Integer> retrievePermissionId(final @NonNull String permissionNode);

    int insertPermission(final @NonNull String permissionNode);
}
