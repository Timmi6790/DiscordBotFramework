package de.timmi6790.discord_framework.modules.permisssion;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static de.timmi6790.discord_framework.AbstractIntegrationTest.MARIA_DB_CONTAINER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PermissionsModuleTest {
    private static final AtomicInteger permissionNodeId = new AtomicInteger(0);

    @Spy
    private final PermissionsModule permissionsModule = new PermissionsModule();

    private static String getPermissionNode() {
        return "test.test." + permissionNodeId.getAndIncrement();
    }

    @BeforeEach
    void setup() {
        final FakeDatabaseModel fakeDatabaseModel = new FakeDatabaseModel(MARIA_DB_CONTAINER);

        doReturn(fakeDatabaseModel).when(this.permissionsModule).getModuleOrThrow(DatabaseModule.class);
        this.permissionsModule.onInitialize();
    }

    @Test
    void hasPermissionId() {
        final String permissionNode = getPermissionNode();

        assertThat(this.permissionsModule.hasPermission(100)).isFalse();

        final int permissionId = this.permissionsModule.addPermission(permissionNode);
        assertThat(this.permissionsModule.hasPermission(permissionId)).isTrue();
    }

    @Test
    void hasPermissionPermissionNode() {
        final String permissionNode = getPermissionNode();

        assertThat(this.permissionsModule.hasPermission(permissionNode)).isFalse();
        this.permissionsModule.addPermission(permissionNode);
        assertThat(this.permissionsModule.hasPermission(permissionNode)).isTrue();
    }

    @Test
    void getPermissionId() {
        final String permissionNode = getPermissionNode();

        final Optional<Integer> permissionNotFound = this.permissionsModule.getPermissionId(permissionNode);
        assertThat(permissionNotFound).isNotPresent();

        this.permissionsModule.addPermission(permissionNode);
        final Optional<Integer> permissionFound = this.permissionsModule.getPermissionId(permissionNode);
        assertThat(permissionFound).isPresent();
    }

    @Test
    void getPermissionFromId() {
        final String permissionNode = getPermissionNode();

        final int permissionId = this.permissionsModule.addPermission(permissionNode);
        final Optional<String> permissionNodeFound = this.permissionsModule.getPermissionFromId(permissionId);
        assertThat(permissionNodeFound)
                .isPresent()
                .contains(permissionNode);
    }

    @Test
    void addPermissionCache() {
        final String permissionNode = getPermissionNode();
        final int permissionIdFirst = this.permissionsModule.addPermission(permissionNode);
        final int permissionIdSecond = this.permissionsModule.addPermission(permissionNode);

        assertThat(permissionIdFirst).isEqualTo(permissionIdSecond);
    }
}