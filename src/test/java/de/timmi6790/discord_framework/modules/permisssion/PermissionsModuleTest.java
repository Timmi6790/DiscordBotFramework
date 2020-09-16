package de.timmi6790.discord_framework.modules.permisssion;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class PermissionsModuleTest {
    private static final AtomicInteger permissionNodeId = new AtomicInteger(0);

    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = (MariaDBContainer) new MariaDBContainer().withClasspathResourceMapping(
            "tables.sql",
            "/docker-entrypoint-initdb.d/createTables.sql",
            BindMode.READ_ONLY
    );

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
}