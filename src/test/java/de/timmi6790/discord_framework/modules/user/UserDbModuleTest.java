package de.timmi6790.discord_framework.modules.user;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class UserDbModuleTest {
    private static final long TEST_DISCORD_ID = 305911488697204736L;
    private static final long TEST_DISCORD_ID2 = 168049519831810048L;

    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = (MariaDBContainer) new MariaDBContainer().withClasspathResourceMapping(
            "tables.sql",
            "/docker-entrypoint-initdb.d/createTables.sql",
            BindMode.READ_ONLY
    );
    private static UserDbModule userDbModule;

    @BeforeAll
    static void setUp() {
        userDbModule = new UserDbModule();

        userDbModule.database = Jdbi.create(MARIA_DB_CONTAINER.getJdbcUrl(), MARIA_DB_CONTAINER.getUsername(), MARIA_DB_CONTAINER.getPassword());
        userDbModule.database.registerRowMapper(UserDb.class, new UserDbMapper(userDbModule.database));
    }

    @Test
    void get() {
        final Optional<UserDb> userDbNotFound = userDbModule.get(TEST_DISCORD_ID);
        assertThat(userDbNotFound).isNotPresent();

        userDbModule.create(TEST_DISCORD_ID);
        final Optional<UserDb> userDbFound = userDbModule.get(TEST_DISCORD_ID);
        assertThat(userDbFound).isPresent();
        assertThat(userDbFound.get().getDiscordId()).isEqualTo(TEST_DISCORD_ID);
    }

    @Test
    void getOrCreate() {
        // Should create them
        final UserDb userDb = userDbModule.getOrCreate(TEST_DISCORD_ID2);
        assertThat(userDb).isNotNull();
        assertThat(userDb.getDiscordId()).isEqualTo(TEST_DISCORD_ID2);

        // Should get it without creation
        final UserDb userDb2 = userDbModule.getOrCreate(TEST_DISCORD_ID2);
        assertThat(userDb2).isNotNull();
        assertThat(userDb2.getDiscordId()).isEqualTo(TEST_DISCORD_ID2);
    }

    @Test
    void deleteUser() {
        final UserDb userDb = userDbModule.getOrCreate(TEST_DISCORD_ID);
        userDbModule.delete(userDb);

        final Optional<UserDb> deletedUser = userDbModule.get(TEST_DISCORD_ID);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void deleteId() {
        final UserDb userDb = userDbModule.getOrCreate(TEST_DISCORD_ID);
        userDbModule.delete(userDb.getDiscordId());

        final Optional<UserDb> deletedUser = userDbModule.get(TEST_DISCORD_ID);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void deleteUserNullCheck() {
        assertThrows(IllegalArgumentException.class, () -> userDbModule.delete(null));
    }

    @Test
    void checkIncorrectCache() {
        userDbModule.create(TEST_DISCORD_ID);
        final UserDb cachedUser = userDbModule.getCache().getIfPresent(TEST_DISCORD_ID);

        userDbModule.getCache().invalidate(TEST_DISCORD_ID);
        final UserDb dbUser = userDbModule.getOrCreate(TEST_DISCORD_ID);

        assertThat(cachedUser).isEqualTo(dbUser);
    }
}