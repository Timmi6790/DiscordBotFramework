package de.timmi6790.discord_framework.modules.user;

import org.assertj.core.api.AssertionsForClassTypes;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Testcontainers
class UserDbTest {
    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = (MariaDBContainer) new MariaDBContainer().withClasspathResourceMapping(
            "tables.sql",
            "/docker-entrypoint-initdb.d/createTables.sql",
            BindMode.READ_ONLY
    );

    private static final AtomicLong DISCORD_ID = new AtomicLong(0);
    private static UserDbModule userDbModule;

    @BeforeAll
    static void setUp() {
        userDbModule = new UserDbModule();

        userDbModule.database = Jdbi.create(MARIA_DB_CONTAINER.getJdbcUrl(), MARIA_DB_CONTAINER.getUsername(), MARIA_DB_CONTAINER.getPassword());
        userDbModule.database.registerRowMapper(UserDb.class, new UserDbMapper(userDbModule.database));
    }

    private static UserDb getUserDbInvalidate(final long discordId) {
        userDbModule.getCache().invalidate(discordId);
        return userDbModule.getOrCreate(discordId);
    }

    @Test
    void setBanned() {
        final long discordId = DISCORD_ID.getAndIncrement();

        // Ban user
        {
            final UserDb userDb = getUserDbInvalidate(discordId);
            assertThat(userDb.isBanned()).isFalse();

            assertThat(userDb.setBanned(true)).isTrue();
            assertThat(userDb.setBanned(true)).isFalse();

            assertThat(userDb.isBanned()).isTrue();
        }

        // Invalidate the cache and get a the same user fresh and unban
        {
            final UserDb userDb = getUserDbInvalidate(discordId);
            assertThat(userDb.isBanned()).isTrue();

            assertThat(userDb.setBanned(false)).isTrue();
            assertThat(userDb.setBanned(false)).isFalse();

            assertThat(userDb.isBanned()).isFalse();
        }

        // Invalidate the cache and check if he is still banned
        {
            final UserDb userDb = getUserDbInvalidate(discordId);
            AssertionsForClassTypes.assertThat(userDb.isBanned()).isFalse();
        }
    }
}