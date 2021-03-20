package de.timmi6790.discord_framework.modules.dsgvo;

import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.new_module_manager.ModuleManager;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class DsgvoModuleTest {
    private static final long TEST_DISCORD_ID = 305911088697004736L;
    private static final long TEST_DISCORD_ID_2 = 102911088697004736L;

    private static final UserDbModule USER_DB_MODULE = spy(UserDbModule.class);
    private static final EventModule EVENT_MODULE = new EventModule();
    private static final DsgvoModule DSGVO_MODULE = spy(DsgvoModule.class);
    private static final ModuleManager MODULE_MANAGER = mock(ModuleManager.class);

    @BeforeAll
    static void setUp() {

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