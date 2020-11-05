package de.timmi6790.discord_framework.modules.user;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class UserDbModuleTest {
    private static final long TEST_DISCORD_ID = 305911488697204736L;
    private static final long TEST_DISCORD_ID2 = 168049519831810048L;

    private static final UserDbModule userDbModule = spy(new UserDbModule());

    @BeforeAll
    static void setUp() {
        final ModuleManager moduleManager = mock(ModuleManager.class);

        final CommandModule commandModule = spy(new CommandModule());
        doNothing().when(commandModule).registerCommands(any(), any());

        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);
        when(moduleManager.getModuleOrThrow(UserDbModule.class)).thenReturn(userDbModule);


        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            userDbModule.onInitialize();
        }
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
    void checkIncorrectCache() {
        userDbModule.create(TEST_DISCORD_ID);
        final UserDb cachedUser = userDbModule.getCache().getIfPresent(TEST_DISCORD_ID);

        userDbModule.getCache().invalidate(TEST_DISCORD_ID);
        final UserDb dbUser = userDbModule.getOrCreate(TEST_DISCORD_ID);

        assertThat(cachedUser).isEqualTo(dbUser);
    }
}