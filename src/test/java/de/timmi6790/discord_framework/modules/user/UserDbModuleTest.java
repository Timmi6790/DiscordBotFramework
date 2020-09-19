package de.timmi6790.discord_framework.modules.user;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.fake_modules.FakeEmptyCommandModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.timmi6790.discord_framework.AbstractIntegrationTest.MARIA_DB_CONTAINER;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class UserDbModuleTest {
    private static final long TEST_DISCORD_ID = 305911488697204736L;
    private static final long TEST_DISCORD_ID2 = 168049519831810048L;

    @Spy
    private final UserDbModule userDbModule = new UserDbModule();

    @BeforeEach
    void setUp() {
        final FakeDatabaseModel fakeDatabaseModel = new FakeDatabaseModel(MARIA_DB_CONTAINER);
        doReturn(fakeDatabaseModel).when(this.userDbModule).getModuleOrThrow(DatabaseModule.class);

        doReturn(new FakeEmptyCommandModule()).when(this.userDbModule).getModuleOrThrow(CommandModule.class);
        this.userDbModule.onInitialize();
    }

    @Test
    void get() {
        final Optional<UserDb> userDbNotFound = this.userDbModule.get(TEST_DISCORD_ID);
        assertThat(userDbNotFound).isNotPresent();

        this.userDbModule.create(TEST_DISCORD_ID);
        final Optional<UserDb> userDbFound = this.userDbModule.get(TEST_DISCORD_ID);
        assertThat(userDbFound).isPresent();
        assertThat(userDbFound.get().getDiscordId()).isEqualTo(TEST_DISCORD_ID);
    }

    @Test
    void getOrCreate() {
        // Should create them
        final UserDb userDb = this.userDbModule.getOrCreate(TEST_DISCORD_ID2);
        assertThat(userDb).isNotNull();
        assertThat(userDb.getDiscordId()).isEqualTo(TEST_DISCORD_ID2);

        // Should get it without creation
        final UserDb userDb2 = this.userDbModule.getOrCreate(TEST_DISCORD_ID2);
        assertThat(userDb2).isNotNull();
        assertThat(userDb2.getDiscordId()).isEqualTo(TEST_DISCORD_ID2);
    }

    @Test
    void deleteUser() {
        final UserDb userDb = this.userDbModule.getOrCreate(TEST_DISCORD_ID);
        this.userDbModule.delete(userDb);

        final Optional<UserDb> deletedUser = this.userDbModule.get(TEST_DISCORD_ID);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void deleteId() {
        final UserDb userDb = this.userDbModule.getOrCreate(TEST_DISCORD_ID);
        this.userDbModule.delete(userDb.getDiscordId());

        final Optional<UserDb> deletedUser = this.userDbModule.get(TEST_DISCORD_ID);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void checkIncorrectCache() {
        this.userDbModule.create(TEST_DISCORD_ID);
        final UserDb cachedUser = this.userDbModule.getCache().getIfPresent(TEST_DISCORD_ID);

        this.userDbModule.getCache().invalidate(TEST_DISCORD_ID);
        final UserDb dbUser = this.userDbModule.getOrCreate(TEST_DISCORD_ID);

        assertThat(cachedUser).isEqualTo(dbUser);
    }
}