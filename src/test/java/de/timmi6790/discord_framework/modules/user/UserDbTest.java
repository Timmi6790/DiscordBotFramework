package de.timmi6790.discord_framework.modules.user;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.fake_modules.FakeEmptyCommandModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicLong;

import static de.timmi6790.discord_framework.AbstractIntegrationTest.MARIA_DB_CONTAINER;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class UserDbTest {
    private static final AtomicLong DISCORD_ID = new AtomicLong(0);
    @Spy
    private final UserDbModule userDbModule = new UserDbModule();

    @BeforeEach
    void setUp() {
        final FakeDatabaseModel fakeDatabaseModel = new FakeDatabaseModel(MARIA_DB_CONTAINER);
        doReturn(fakeDatabaseModel).when(this.userDbModule).getModuleOrThrow(DatabaseModule.class);

        doReturn(new FakeEmptyCommandModule()).when(this.userDbModule).getModuleOrThrow(CommandModule.class);
        this.userDbModule.onInitialize();
    }

    private UserDb getUserDbInvalidate(final long discordId) {
        this.userDbModule.getCache().invalidate(discordId);
        return this.userDbModule.getOrCreate(discordId);
    }

    @Test
    void setBanned() {
        final long discordId = DISCORD_ID.getAndIncrement();

        // Ban user
        {
            final UserDb userDb = this.getUserDbInvalidate(discordId);
            assertThat(userDb.isBanned()).isFalse();

            assertThat(userDb.setBanned(true)).isTrue();
            assertThat(userDb.setBanned(true)).isFalse();

            assertThat(userDb.isBanned()).isTrue();
        }

        // Invalidate the cache and get a the same user fresh and unban
        {
            final UserDb userDb = this.getUserDbInvalidate(discordId);
            assertThat(userDb.isBanned()).isTrue();

            assertThat(userDb.setBanned(false)).isTrue();
            assertThat(userDb.setBanned(false)).isFalse();

            assertThat(userDb.isBanned()).isFalse();
        }

        // Invalidate the cache and check if he is still banned
        {
            final UserDb userDb = this.getUserDbInvalidate(discordId);
            AssertionsForClassTypes.assertThat(userDb.isBanned()).isFalse();
        }
    }
}