package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RankTest {
    private static final String PERMISSION_PREFIX = "rank_test.";
    private static final AtomicInteger RANK_NAME_NUMBER = new AtomicInteger(0);

    private static final RankModule rankModule = spy(new RankModule());
    private static final PermissionsModule permissionsModule = spy(new PermissionsModule());
    private static final UserDbModule userDbModule = spy(new UserDbModule());

    private static final Set<Integer> permissionIds = new HashSet<>();

    @BeforeAll
    static void setup() {
        final ModuleManager moduleManager = mock(ModuleManager.class);

        final CommandModule commandModule = spy(new CommandModule());
        doNothing().when(commandModule).registerCommands(any(), any());

        when(moduleManager.getModuleOrThrow(PermissionsModule.class)).thenReturn(permissionsModule);
        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);
        when(moduleManager.getModuleOrThrow(RankModule.class)).thenReturn(rankModule);
        when(moduleManager.getModuleOrThrow(UserDbModule.class)).thenReturn(userDbModule);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            permissionsModule.onInitialize();
            userDbModule.onInitialize();
            rankModule.onInitialize();
        }

        for (int count = 0; 10 > count; count++) {
            permissionIds.add(permissionsModule.addPermission(PERMISSION_PREFIX + count));
        }
    }

    private String getRankName() {
        return "RandomRankRank" + RANK_NAME_NUMBER.getAndIncrement();
    }

    private Rank createRank() {
        final String rankName = this.getRankName();
        rankModule.createRank(rankName);

        return rankModule.getRank(rankName).orElseThrow(RuntimeException::new);
    }

    @Test
    void hasPermission() {
        final Rank rank = this.createRank();

        for (final int permission : permissionIds) {
            assertThat(rank.hasPermission(permission)).isFalse();
        }

        for (final int permission : permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }

        for (final int permission : permissionIds) {
            assertThat(rank.hasPermission(permission)).isTrue();
        }
    }

    @Test
    void addPermission() {
        final Rank rank = this.createRank();

        assertThat(rank.getPermissionIds().toArray(new Integer[0])).isEmpty();
        for (final int permission : permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }
        assertThat(rank.getPermissionIds()).containsExactlyInAnyOrderElementsOf(permissionIds);
    }

    @Test
    void addPermission_duplicate() {
        final Rank rank = this.createRank();

        final int permissionId = permissionIds.toArray(new Integer[0])[0];
        assertThat(rank.addPermission(permissionId)).isTrue();
        assertThat(rank.addPermission(permissionId)).isFalse();
    }

    @Test
    void removePermission() {
        final Rank rank = this.createRank();

        assertThat(rank.getPermissionIds().toArray(new Integer[0])).isEmpty();
        for (final int permission : permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }

        for (final int permission : permissionIds) {
            assertThat(rank.removePermission(permission)).isTrue();
        }
        assertThat(rank.getPermissionIds()).isEmpty();
    }

    @Test
    void addPermission_not_Existing() {
        final Rank rank = this.createRank();

        final int permissionId = permissionIds.toArray(new Integer[0])[0];
        assertThat(rank.removePermission(permissionId)).isFalse();
    }

    @Test
    void getAllPermissions() {
        final Set<Integer> permissionIds = new HashSet<>();
        final Set<String> permissions = new HashSet<>();

        // Extended Rank 3
        final Rank extendedRank3 = this.createRank();

        final String rank3permissionNode = PERMISSION_PREFIX + extendedRank3.getName();
        final int rank3permission = permissionsModule.addPermission(rank3permissionNode);
        permissionIds.add(rank3permission);
        permissions.add(rank3permissionNode);

        extendedRank3.addPermission(rank3permission);

        // Extended Rank 2
        final Rank extendedRank2 = this.createRank();
        extendedRank2.addExtendedRank(extendedRank3);

        final String rank2permissionNode = PERMISSION_PREFIX + extendedRank2.getName();
        final int rank2permission = permissionsModule.addPermission(rank2permissionNode);
        permissionIds.add(rank2permission);
        permissions.add(rank2permissionNode);

        extendedRank2.addPermission(rank2permission);

        // Extended Rank 1
        final Rank extendedRank1 = this.createRank();

        final String rank1permissionNode = PERMISSION_PREFIX + extendedRank1.getName();
        final int rank1permission = permissionsModule.addPermission(rank1permissionNode);
        permissionIds.add(rank1permission);
        permissions.add(rank1permissionNode);

        extendedRank1.addPermission(rank1permission);

        // Main rank
        final Rank mainRank = this.createRank();
        mainRank.addExtendedRank(extendedRank1);
        mainRank.addExtendedRank(extendedRank2);

        final String mainRankPermissionNode = PERMISSION_PREFIX + mainRank.getName();
        final int mainRankPermission = permissionsModule.addPermission(mainRankPermissionNode);
        permissionIds.add(mainRankPermission);
        permissions.add(mainRankPermissionNode);

        mainRank.addPermission(mainRankPermission);
        mainRank.addPermission(rank3permission);


        assertThat(mainRank.getAllPermissionIds()).containsExactlyInAnyOrderElementsOf(permissionIds);
        assertThat(mainRank.getAllPermissions()).containsExactlyInAnyOrderElementsOf(permissions);

        // Cache check
        assertThat(mainRank.getAllPermissionIds()).containsExactlyInAnyOrderElementsOf(permissionIds);
        assertThat(mainRank.getAllPermissions()).containsExactlyInAnyOrderElementsOf(permissions);
    }

    @Test
    void hasExtendedRank() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();

        assertThat(mainRank.hasExtendedRank(extendedRank1)).isFalse();
        mainRank.addExtendedRank(extendedRank1);
        assertThat(mainRank.hasExtendedRank(extendedRank1)).isTrue();
    }

    @Test
    void hasExtendedRank_id() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();

        assertThat(mainRank.hasExtendedRank(extendedRank1.getDatabaseId())).isFalse();
        mainRank.addExtendedRank(extendedRank1);
        assertThat(mainRank.hasExtendedRank(extendedRank1.getDatabaseId())).isTrue();
    }

    @Test
    void addExtendedRank() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();

        assertThat(mainRank.addExtendedRank(extendedRank1)).isTrue();
        assertThat(mainRank.addExtendedRank(extendedRank1)).isFalse();
        assertThat(mainRank.addExtendedRank(mainRank)).isFalse();
    }

    @Test
    void addExtendedRank_id() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();

        assertThat(mainRank.addExtendedRank(extendedRank1.getDatabaseId())).isTrue();
        assertThat(mainRank.addExtendedRank(extendedRank1.getDatabaseId())).isFalse();
        assertThat(mainRank.addExtendedRank(mainRank.getDatabaseId())).isFalse();
    }

    @Test
    void removeExtendedRank() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();

        assertThat(mainRank.removeExtendedRank(extendedRank1)).isFalse();
        mainRank.addExtendedRank(extendedRank1);
        assertThat(mainRank.removeExtendedRank(extendedRank1)).isTrue();
        assertThat(mainRank.removeExtendedRank(extendedRank1)).isFalse();
    }

    @Test
    void removeExtendedRank_id() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();

        mainRank.addExtendedRank(extendedRank1);
        assertThat(mainRank.removeExtendedRank(extendedRank1.getDatabaseId())).isTrue();
        assertThat(mainRank.removeExtendedRank(extendedRank1.getDatabaseId())).isFalse();
    }

    @Test
    void setName() {
        final String newName = "_A";

        final Rank rank = this.createRank();
        final String currentName = rank.getName();

        assertThat(rank.setName(newName)).isTrue();
        assertThat(rank.setName(newName)).isFalse();

        assertThat(rank.getName())
                .isNotEqualTo(currentName)
                .isEqualTo(newName);
    }

    @Test
    void retrievePlayerCount() {
        final UserDb user1 = userDbModule.getOrCreate(1);
        final UserDb user2 = userDbModule.getOrCreate(2);

        final Rank rank = this.createRank();
        assertThat(rank.retrievePlayerCount()).isZero();

        user1.setPrimaryRank(rank);
        user2.setPrimaryRank(rank);
        assertThat(rank.retrievePlayerCount()).isEqualTo(2);
    }

    @Test
    void retrieveAllPlayers() {
        final Rank rank = this.createRank();
        assertThat(rank.retrieveAllPlayers().toArray(new UserDb[0])).isEmpty();

        final List<UserDb> userDbList = new ArrayList<>();
        for (int count = 0; 10 > count; count++) {
            final UserDb userDb = userDbModule.getOrCreate(count);
            userDb.setPrimaryRank(rank);
            userDbList.add(userDb);
        }

        assertThat(rank.retrieveAllPlayers()).containsExactlyInAnyOrderElementsOf(userDbList);
    }
}