package de.timmi6790.discord_framework.modules.user;

import de.timmi6790.discord_framework.modules.event.EventModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.spy;

class UserDbModuleTest {
    private static final long TEST_DISCORD_ID = 305911488697204736L;
    private static final long TEST_DISCORD_ID2 = 168049519831810048L;

    private static final UserDbModule USER_DB_MODULE = spy(UserDbModule.class);
    private static final EventModule EVENT_MODULE = new EventModule();

    @BeforeAll
    static void setUp() {

    }

    @Test
    void get() {
        final Optional<UserDb> userDbNotFound = USER_DB_MODULE.get(TEST_DISCORD_ID);
        assertThat(userDbNotFound).isNotPresent();

        USER_DB_MODULE.create(TEST_DISCORD_ID);
        final Optional<UserDb> userDbFound = USER_DB_MODULE.get(TEST_DISCORD_ID);
        assertThat(userDbFound).isPresent();
        assertThat(userDbFound.get().getDiscordId()).isEqualTo(TEST_DISCORD_ID);
    }

    @Test
    void getOrCreate() {
        // Should create them
        final UserDb userDb = USER_DB_MODULE.getOrCreate(TEST_DISCORD_ID2);
        assertThat(userDb).isNotNull();
        assertThat(userDb.getDiscordId()).isEqualTo(TEST_DISCORD_ID2);

        // Should get it without creation
        final UserDb userDb2 = USER_DB_MODULE.getOrCreate(TEST_DISCORD_ID2);
        assertThat(userDb2).isNotNull();
        assertThat(userDb2.getDiscordId()).isEqualTo(TEST_DISCORD_ID2);
    }

    @Test
    void deleteUser() {
        final UserDb userDb = USER_DB_MODULE.getOrCreate(TEST_DISCORD_ID);
        USER_DB_MODULE.delete(userDb);

        final Optional<UserDb> deletedUser = USER_DB_MODULE.get(TEST_DISCORD_ID);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void deleteId() {
        final UserDb userDb = USER_DB_MODULE.getOrCreate(TEST_DISCORD_ID);
        USER_DB_MODULE.delete(userDb.getDiscordId());

        final Optional<UserDb> deletedUser = USER_DB_MODULE.get(TEST_DISCORD_ID);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void checkIncorrectCache() {
        USER_DB_MODULE.create(TEST_DISCORD_ID);
        final UserDb cachedUser = USER_DB_MODULE.getCache().getIfPresent(TEST_DISCORD_ID);

        USER_DB_MODULE.getCache().invalidate(TEST_DISCORD_ID);
        final UserDb dbUser = USER_DB_MODULE.getOrCreate(TEST_DISCORD_ID);

        assertThat(cachedUser).isEqualTo(dbUser);
    }
}