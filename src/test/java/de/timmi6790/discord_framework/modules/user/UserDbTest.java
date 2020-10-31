package de.timmi6790.discord_framework.modules.user;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserDbTest {
    private static final AtomicLong DISCORD_ID = new AtomicLong(0);
    @Spy
    private static final UserDbModule userDbModule = Mockito.spy(new UserDbModule());

    @BeforeAll
    static void setUp() {
        doReturn(AbstractIntegrationTest.databaseModule).when(userDbModule).getModuleOrThrow(DatabaseModule.class);

        final CommandModule commandModule = spy(new CommandModule());
        doNothing().when(commandModule).registerCommands(any(), any());
        doReturn(commandModule).when(userDbModule).getModuleOrThrow(CommandModule.class);
        userDbModule.onInitialize();
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