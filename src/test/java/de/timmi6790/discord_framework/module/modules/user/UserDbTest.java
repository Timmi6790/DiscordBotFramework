package de.timmi6790.discord_framework.module.modules.user;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.setting.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.module.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.module.modules.stat.StatModule;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class UserDbTest {
    private static final AtomicInteger DISCORD_ID = new AtomicInteger(1_000);
    private static final AtomicInteger PERMISSION_ID = new AtomicInteger(0);
    private static final AtomicInteger RANK_ID = new AtomicInteger(0);

    private static final ModuleManager moduleManager = mock(ModuleManager.class);
    private static final RankModule rankModule = spy(new RankModule());
    private static final UserDbModule userDbModule = spy(new UserDbModule());
    private static final PermissionsModule permissionsModule = spy(new PermissionsModule());
    private static final AchievementModule achievementModule = spy(new AchievementModule());
    private static final SettingModule settingModule = spy(new SettingModule());
    private static final StatModule statModule = spy(new StatModule());

    @BeforeAll
    static void setUp() {
        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getModuleManager()).thenReturn(moduleManager);

        final EventModule eventModule = mock(EventModule.class);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(eventModule);

        when(moduleManager.getModuleOrThrow(PermissionsModule.class)).thenReturn(permissionsModule);
        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);
        when(moduleManager.getModuleOrThrow(RankModule.class)).thenReturn(rankModule);
        when(moduleManager.getModuleOrThrow(AchievementModule.class)).thenReturn(achievementModule);
        when(moduleManager.getModule(AchievementModule.class)).thenReturn(Optional.of(achievementModule));
        when(moduleManager.getModuleOrThrow(UserDbModule.class)).thenReturn(userDbModule);
        when(moduleManager.getModule(SettingModule.class)).thenReturn(Optional.of(settingModule));
        when(moduleManager.getModule(StatModule.class)).thenReturn(Optional.of(statModule));

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            final JDA jda = mock(JDA.class);
            when(jda.getResponseTotal()).thenReturn(1L);

            final ShardManager discord = mock(ShardManager.class);
            when(discord.getShardById(0)).thenReturn(jda);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            eventModule.onInitialize();
            permissionsModule.onInitialize();
            settingModule.onInitialize();
            achievementModule.onInitialize();
            statModule.onInitialize();
            rankModule.onInitialize();
            userDbModule.onInitialize();
        }
    }

    private UserDb generateUser() {
        return userDbModule.getOrCreate(DISCORD_ID.getAndIncrement());
    }

    private Rank generateRank() {
        final String rankName = "UserDbTestAllPerms-" + RANK_ID.getAndIncrement();

        rankModule.createRank(rankName);
        return rankModule.getRank(rankName).orElseThrow(RuntimeException::new);
    }

    private String generatePermNode() {
        return "user_db_test_" + PERMISSION_ID.getAndIncrement();
    }

    private void validateRepository(final UserDb userDb) {
        final UserDb repositoryUser = userDbModule.getUserDbRepository().get(userDb.getDiscordId()).orElseThrow(RuntimeException::new);
        assertThat(userDb).isEqualTo(repositoryUser);
    }

    private void mockDiscord(final Runnable runnable) {
        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final JDA jda = mock(JDA.class);
            when(jda.getResponseTotal()).thenReturn(1L);

            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getBaseShard()).thenReturn(jda);

            final ShardManager discord = mock(ShardManager.class);
            when(discord.getShardById(0)).thenReturn(jda);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            runnable.run();
        }
    }

    @Test
    void ban() {
        final UserDb userDb = this.generateUser();

        assertThat(userDb.isBanned()).isFalse();

        try (final MockedStatic<DiscordMessagesUtilities> discordMessageMock = mockStatic(DiscordMessagesUtilities.class)) {
            final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
            discordMessageMock.when(() -> DiscordMessagesUtilities.getEmbedBuilder(any())).thenReturn(embedBuilder);

            final CommandParameters commandParameters = mock(CommandParameters.class);
            userDb.ban(commandParameters, "");
        }

        assertThat(userDb.isBanned()).isTrue();

        this.validateRepository(userDb);
    }

    @Test
    void setBanned() {
        final UserDb userDb = this.generateUser();

        // Ban user
        assertThat(userDb.isBanned()).isFalse();

        assertThat(userDb.setBanned(true)).isTrue();
        assertThat(userDb.setBanned(true)).isFalse();

        assertThat(userDb.isBanned()).isTrue();
        this.validateRepository(userDb);

        // Unban user
        assertThat(userDb.setBanned(false)).isTrue();
        assertThat(userDb.setBanned(false)).isFalse();

        assertThat(userDb.isBanned()).isFalse();
        this.validateRepository(userDb);
    }

    // Permission
    @Test
    void getAllPermissionIds() {
        final int permId1 = permissionsModule.addPermission(this.generatePermNode());
        final int permId2 = permissionsModule.addPermission(this.generatePermNode());
        final int permId3 = permissionsModule.addPermission(this.generatePermNode());

        final Rank rank = this.generateRank();
        rank.addPermission(permId1);
        rank.addPermission(permId2);

        final UserDb userDb = this.generateUser();
        userDb.addRank(rank);
        userDb.addPermission(permId3);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);
            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            assertThat(userDb.getAllPermissionIds()).contains(permId1, permId2, permId3);
        }

        this.validateRepository(userDb);
    }

    @Test
    void addPermission() {
        final String permission = this.generatePermNode();
        final int permissionId = permissionsModule.addPermission(permission);

        final UserDb userDb = this.generateUser();

        assertThat(userDb.hasPermission(permissionId)).isFalse();
        assertThat(userDb.addPermission(permissionId)).isTrue();
        assertThat(userDb.addPermission(permissionId)).isFalse();
        assertThat(userDb.hasPermission(permissionId)).isTrue();

        this.validateRepository(userDb);
    }

    @Test
    void removePermission() {
        final String permission = this.generatePermNode();
        final int permissionId = permissionsModule.addPermission(permission);

        final UserDb userDb = this.generateUser();

        userDb.addPermission(permissionId);

        assertThat(userDb.removePermission(permissionId)).isTrue();
        assertThat(userDb.removePermission(permissionId)).isFalse();

        assertThat(userDb.hasPermission(permissionId)).isFalse();

        this.validateRepository(userDb);
    }

    // Ranks
    @Test
    void setPrimaryRankId_rank() {
        final Rank rank = this.generateRank();
        final UserDb userDb = this.generateUser();

        assertThat(userDb.hasPrimaryRank(rank)).isFalse();

        assertThat(userDb.setPrimaryRank(rank)).isTrue();
        assertThat(userDb.setPrimaryRank(rank)).isFalse();

        assertThat(userDb.hasPrimaryRank(rank)).isTrue();

        this.validateRepository(userDb);
    }

    @Test
    void addRank_rank() {
        final UserDb userDb = this.generateUser();

        final List<Rank> ranks = new ArrayList<>();
        for (int count = 0; 10 > count; count++) {
            final Rank rank = this.generateRank();
            ranks.add(rank);

            assertThat(userDb.hasRank(rank)).isFalse();

            assertThat(userDb.addRank(rank)).isTrue();
            assertThat(userDb.addRank(rank)).isFalse();
        }

        for (final Rank rank : ranks) {
            assertThat(userDb.hasRank(rank)).isTrue();
        }

        this.validateRepository(userDb);
    }

    @Test
    void removeRank_rank() {
        final Rank rank = this.generateRank();

        final UserDb userDb = this.generateUser();
        userDb.addRank(rank);

        assertThat(userDb.removeRank(rank)).isTrue();
        assertThat(userDb.removeRank(rank)).isFalse();

        this.validateRepository(userDb);
    }

    // Achievements
    @Test
    void getAchievements() {
        final UserDb userDb = this.generateUser();

        final TestAchievement achievement = new TestAchievement();
        final TestAchievement2 achievement2 = new TestAchievement2();
        achievementModule.registerAchievements(userDbModule, achievement, achievement2);

        // empty
        assertThat(userDb.getAchievements()).isEmpty();

        // one achievement
        userDb.grantAchievement(achievement, false);
        assertThat(userDb.getAchievements()).containsExactlyInAnyOrder(achievement);

        // two achievement
        userDb.grantAchievement(achievement2, false);
        assertThat(userDb.getAchievements()).containsExactlyInAnyOrder(achievement, achievement2);

        this.validateRepository(userDb);
    }

    @Test
    void hasAchievement() {
        final UserDb userDb = this.generateUser();

        final TestAchievement achievement = spy(new TestAchievement());
        achievementModule.registerAchievement(userDbModule, achievement);

        assertThat(userDb.hasAchievement(achievement)).isFalse();

        assertThat(userDb.grantAchievement(achievement, false)).isTrue();
        verify(achievement).onUnlock(any());

        assertThat(userDb.grantAchievement(achievement, false)).isFalse();

        assertThat(userDb.hasAchievement(achievement)).isTrue();

        this.validateRepository(userDb);
    }

    // Stats
    @Test
    void getStatValue() {
        final UserDb userDb = this.generateUser();

        this.mockDiscord(() -> {
            // Test with one stat
            final TestStat testStat = new TestStat();
            statModule.registerStat(userDbModule, testStat);

            assertThat(userDb.getStatValue(testStat)).isEmpty();

            userDb.increaseStat(testStat);
            assertThat(userDb.getStatValue(testStat)).hasValue(1);

            userDb.increaseStat(testStat);
            assertThat(userDb.getStatValue(testStat)).hasValue(2);

            userDb.setStatValue(testStat, 10);
            assertThat(userDb.getStatValue(testStat)).hasValue(10);

            // Test with second stat
            final TestStat2 testStat2 = new TestStat2();
            statModule.registerStat(userDbModule, testStat2);
            assertThat(userDb.getStatValue(testStat2)).isEmpty();

            userDb.increaseStat(testStat2);
            assertThat(userDb.getStatValue(testStat2)).hasValue(1);
        });

        this.validateRepository(userDb);
    }

    @Test
    void getStatsMap() {
        final UserDb userDb = this.generateUser();

        // Empty check
        assertThat(userDb.getStatsMap()).isEmpty();

        this.mockDiscord(() -> {
            // One stat
            final TestStat testStat = new TestStat();
            statModule.registerStat(userDbModule, testStat);
            userDb.increaseStat(testStat);
            assertThat(userDb.getStatsMap()).containsExactlyInAnyOrderEntriesOf(
                    Map.of(testStat, 1)
            );

            // Two stats
            final TestStat2 testStat2 = new TestStat2();
            statModule.registerStat(userDbModule, testStat2);
            userDb.setStatValue(testStat2, 290);
            assertThat(userDb.getStatsMap()).containsExactlyInAnyOrderEntriesOf(
                    Map.of(testStat, 1,
                            testStat2, 290)
            );
        });


        this.validateRepository(userDb);
    }

    // Settings
    @Test
    void getSettings() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());

        final UserDb userDb = this.generateUser();

        // Empty
        assertThat(userDb.getSettings()).isEmpty();

        // One Entry
        userDb.grantSetting(CommandAutoCorrectSetting.class);
        assertThat(userDb.getSettings()).hasSize(1);

        this.validateRepository(userDb);
    }

    @Test
    void hasSetting_class() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());

        final UserDb userDb = this.generateUser();

        assertThat(userDb.hasSetting(CommandAutoCorrectSetting.class)).isFalse();
        userDb.grantSetting(CommandAutoCorrectSetting.class);
        assertThat(userDb.hasSetting(CommandAutoCorrectSetting.class)).isTrue();
    }

    @Test
    void getSetting_class() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());
        final UserDb userDb = this.generateUser();

        assertThat(userDb.getSetting(CommandAutoCorrectSetting.class)).isEmpty();
        userDb.grantSetting(CommandAutoCorrectSetting.class);
        assertThat(userDb.getSetting(CommandAutoCorrectSetting.class)).isPresent();

        this.validateRepository(userDb);
    }

    @Test
    void setSetting() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());
        final UserDb userDb = this.generateUser();

        userDb.grantSetting(CommandAutoCorrectSetting.class);
        userDb.setSetting(CommandAutoCorrectSetting.class, true);
        userDb.setSetting(CommandAutoCorrectSetting.class, false);

        this.validateRepository(userDb);
    }

    @Test
    void grantSetting() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());
        final UserDb userDb = this.generateUser();

        assertThat(userDb.grantSetting(CommandAutoCorrectSetting.class)).isTrue();
        assertThat(userDb.grantSetting(CommandAutoCorrectSetting.class)).isFalse();
    }

    @Test
    void hasAutoCorrection_with_granted_perms() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());

        final UserDb userDb = this.generateUser();

        userDb.grantSetting(CommandAutoCorrectSetting.class);

        assertThat(userDb.hasAutoCorrection()).isFalse();
        this.validateRepository(userDb);
    }

    @Test
    void hasAutoCorrection_with_granted_perms_false() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());

        final UserDb userDb = this.generateUser();

        userDb.grantSetting(CommandAutoCorrectSetting.class);
        userDb.setSetting(CommandAutoCorrectSetting.class, false);

        assertThat(userDb.hasAutoCorrection()).isFalse();
        this.validateRepository(userDb);
    }

    @Test
    void hasAutoCorrection_without_granted() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());

        final UserDb userDb = this.generateUser();
        assertThat(userDb.hasAutoCorrection()).isFalse();
        this.validateRepository(userDb);
    }

    @Test
    void hasAutoCorrection_with() {
        settingModule.registerSetting(userDbModule, new CommandAutoCorrectSetting());

        final UserDb userDb = this.generateUser();

        userDb.grantSetting(CommandAutoCorrectSetting.class);
        userDb.setSetting(CommandAutoCorrectSetting.class, true);

        assertThat(userDb.hasAutoCorrection()).isTrue();
        this.validateRepository(userDb);
    }

    private static class TestAchievement extends AbstractAchievement {
        protected TestAchievement() {
            this("test");
        }

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

    private static class TestAchievement2 extends TestAchievement {
        protected TestAchievement2() {
            super("test2");
        }
    }

    private static class TestStat extends AbstractStat {
        protected TestStat() {
            this("test1");
        }

        protected TestStat(final String name) {
            super(name);
        }
    }

    private static class TestStat2 extends TestStat {
        protected TestStat2() {
            super("test2");
        }
    }
}