package de.timmi6790.discord_framework.modules.permisssion;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionsModuleTest {
    private static final AtomicInteger permissionNodeId = new AtomicInteger(0);

    private static final PermissionsModule permissionsModule = Mockito.spy(PermissionsModule.class);

    private static String getPermissionNode() {
        return "test.test." + permissionNodeId.getAndIncrement();
    }

    @BeforeAll
    static void setup() {
      
    }

    @Test
    void hasPermissionId() {
        final String permissionNode = getPermissionNode();

        assertThat(permissionsModule.hasPermission(100)).isFalse();

        final int permissionId = permissionsModule.addPermission(permissionNode);
        assertThat(permissionsModule.hasPermission(permissionId)).isTrue();
    }

    @Test
    void hasPermissionPermissionNode() {
        final String permissionNode = getPermissionNode();

        assertThat(permissionsModule.hasPermission(permissionNode)).isFalse();
        permissionsModule.addPermission(permissionNode);
        assertThat(permissionsModule.hasPermission(permissionNode)).isTrue();
    }

    @Test
    void getPermissionId() {
        final String permissionNode = getPermissionNode();

        final Optional<Integer> permissionNotFound = permissionsModule.getPermissionId(permissionNode);
        assertThat(permissionNotFound).isNotPresent();

        permissionsModule.addPermission(permissionNode);
        final Optional<Integer> permissionFound = permissionsModule.getPermissionId(permissionNode);
        assertThat(permissionFound).isPresent();
    }

    @Test
    void getPermissionFromId() {
        final String permissionNode = getPermissionNode();

        final int permissionId = permissionsModule.addPermission(permissionNode);
        final Optional<String> permissionNodeFound = permissionsModule.getPermissionFromId(permissionId);
        assertThat(permissionNodeFound)
                .isPresent()
                .contains(permissionNode);
    }

    @Test
    void addPermissionCache() {
        final String permissionNode = getPermissionNode();
        final int permissionIdFirst = permissionsModule.addPermission(permissionNode);
        final int permissionIdSecond = permissionsModule.addPermission(permissionNode);

        assertThat(permissionIdFirst).isEqualTo(permissionIdSecond);
    }
}