package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.modules.user.UserDb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.spy;

class AchievementModuleTest {
    private static final AtomicInteger SETTING_NAME_NUMBER = new AtomicInteger(0);

    private static final AchievementModule achievementModule = spy(AchievementModule.class);

    private static String generateAchievementName() {
        return "Achievement" + SETTING_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {

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
        System.out.println(achievement);
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