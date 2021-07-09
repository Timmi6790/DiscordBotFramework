package de.timmi6790.discord_framework.module.modules.achievement;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class AchievementModuleTest {
    private static final AtomicInteger SETTING_NAME_NUMBER = new AtomicInteger(0);

    private static final AchievementModule achievementModule = spy(new AchievementModule());

    private static String generateAchievementName() {
        return "Achievement" + SETTING_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {
        final ModuleManager moduleManager = mock(ModuleManager.class);
        final PermissionsModule permissionsModule = spy(new PermissionsModule());
        doReturn(permissionsModule).when(moduleManager).getModuleOrThrow(PermissionsModule.class);
        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getModuleManager()).thenReturn(moduleManager);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);

        final EventModule eventModule = mock(EventModule.class);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(eventModule);

        doReturn(moduleManager).when(permissionsModule).getModuleManager();
        doReturn(moduleManager).when(achievementModule).getModuleManager();

        permissionsModule.onInitialize();
        achievementModule.onInitialize();
    }

    private void hasAchievements(final AbstractAchievement... achievements) {
        for (final AbstractAchievement achievement : achievements) {
            assertThat(achievementModule.getAchievement(achievement.getRepositoryId())).hasValue(achievement);
            assertThat(achievementModule.getAchievement(achievement.getAchievementName())).hasValue(achievement);
        }
    }

    @Test
    void registerAchievements() {
        final TestAchievement achievement1 = new TestAchievement(generateAchievementName());
        final TestAchievement achievement2 = new TestAchievement(generateAchievementName());

        achievementModule.registerAchievements(
                achievementModule,
                achievement1,
                achievement2
        );
        this.hasAchievements(achievement1, achievement2);
    }

    @Test
    void registerAchievement_duplicate() {
        final TestAchievement achievement = new TestAchievement(generateAchievementName());
        assertThat(achievementModule.registerAchievement(achievementModule, achievement)).isTrue();
        assertThat(achievementModule.registerAchievement(achievementModule, achievement)).isFalse();
    }

    @Test
    void getAchievement_name_empty() {
        assertThat(achievementModule.getAchievement(generateAchievementName())).isEmpty();
    }

    @Test
    void getAchievement() {
        final TestAchievement achievement = new TestAchievement(generateAchievementName());
        achievementModule.registerAchievement(achievementModule, achievement);
        this.hasAchievements(achievement);
    }

    private static class TestAchievement extends AbstractAchievement {
        protected TestAchievement(final String name) {
            super(name);
        }

        @Override
        public void onUnlock(final UserDb userDb) {

        }

        @Override
        public List<String> getUnlockedPerks() {
            return null;
        }
    }
}