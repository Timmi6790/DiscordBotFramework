package de.timmi6790.discord_framework.modules.user;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class UserDbModuleTest {
    private static final long TEST_DISCORD_ID = 305911488697204736L;
    private static final long TEST_DISCORD_ID2 = 168049519831810048L;

    private static final UserDbModule USER_DB_MODULE = spy(new UserDbModule());
    private static final EventModule EVENT_MODULE = new EventModule();

    @BeforeAll
    static void setUp() {
        final ModuleManager moduleManager = mock(ModuleManager.class);

        final CommandModule commandModule = spy(new CommandModule());
        doNothing().when(commandModule).registerCommands(any(), any());

        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);
        when(moduleManager.getModuleOrThrow(UserDbModule.class)).thenReturn(USER_DB_MODULE);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(EVENT_MODULE);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            final ShardManager discord = mock(ShardManager.class);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            USER_DB_MODULE.onInitialize();
        }
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