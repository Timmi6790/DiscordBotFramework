package de.timmi6790.discord_framework.module.modules.dsgvo;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DsgvoModuleTest {
    private static final long TEST_DISCORD_ID = 305911088697004736L;
    private static final long TEST_DISCORD_ID_2 = 102911088697004736L;

    private static final UserDbModule USER_DB_MODULE = spy(new UserDbModule());
    private static final EventModule EVENT_MODULE = new EventModule();
    private static final DsgvoModule DSGVO_MODULE = spy(new DsgvoModule());
    private static final ModuleManager MODULE_MANAGER = mock(ModuleManager.class);

    @BeforeAll
    static void setUp() {
        final CommandModule commandModule = spy(new CommandModule());
        doNothing().when(commandModule).registerCommands(any(), any());

        doReturn(AbstractIntegrationTest.databaseModule).when(MODULE_MANAGER).getModuleOrThrow(DatabaseModule.class);
        when(MODULE_MANAGER.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);
        when(MODULE_MANAGER.getModuleOrThrow(UserDbModule.class)).thenReturn(USER_DB_MODULE);
        when(MODULE_MANAGER.getModuleOrThrow(EventModule.class)).thenReturn(EVENT_MODULE);
        when(MODULE_MANAGER.getModuleOrThrow(DsgvoModule.class)).thenReturn(DSGVO_MODULE);

        final DiscordBot discordBot = mock(DiscordBot.class);
        final JDA jda = mock(JDA.class);
        when(discordBot.getBaseShard()).thenReturn(jda);

        doReturn(discordBot).when(DSGVO_MODULE).getDiscordBot();
        doReturn(EVENT_MODULE).when(DSGVO_MODULE).getModuleOrThrow(EventModule.class);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(MODULE_MANAGER);

            final ShardManager discord = mock(ShardManager.class);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            USER_DB_MODULE.onInitialize();
        }
    }

    @Test
    @Disabled("Concurrent exception")
    void getUserData() {
        final UserDb userDb = USER_DB_MODULE.getOrCreate(TEST_DISCORD_ID);

        final String userData = DSGVO_MODULE.getUserData(userDb);
        assertThat(userData).isNotEmpty();
    }

    @Test
    void deleteUserData() {
        final UserDb userDb = USER_DB_MODULE.getOrCreate(TEST_DISCORD_ID_2);
        DSGVO_MODULE.deleteUserData(userDb);

        // This is event based. We need to forcefully wait to see the effect
        Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .until(() -> !USER_DB_MODULE.get(TEST_DISCORD_ID_2).isPresent());

        assertThat(USER_DB_MODULE.get(TEST_DISCORD_ID_2)).isNotPresent();
    }
}