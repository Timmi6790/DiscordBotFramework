package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.fake_modules.FakeEmptyCommandModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RankTest {
    private static final String PERMISSION_PREFIX = "rank_test.";
    private static final AtomicInteger RANK_NAME_NUMBER = new AtomicInteger(0);

    @Spy
    private static final RankModule rankModule = Mockito.spy(new RankModule());
    @Spy
    private static final PermissionsModule permissionsModule = Mockito.spy(new PermissionsModule());
    @Spy
    private static final UserDbModule userDbModule = Mockito.spy(new UserDbModule());

    private static final Set<Integer> permissionIds = new HashSet<>();

    private static String getRankName() {
        return "RandomRankRank" + RANK_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {
        doReturn(AbstractIntegrationTest.databaseModule).when(permissionsModule).getModuleOrThrow(DatabaseModule.class);
        permissionsModule.onInitialize();

        doReturn(AbstractIntegrationTest.databaseModule).when(userDbModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(new FakeEmptyCommandModule()).when(userDbModule).getModuleOrThrow(CommandModule.class);
        userDbModule.onInitialize();

        doReturn(AbstractIntegrationTest.databaseModule).when(rankModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(userDbModule).when(rankModule).getModuleOrThrow(UserDbModule.class);
        doReturn(new FakeEmptyCommandModule()).when(rankModule).getModuleOrThrow(CommandModule.class);

        rankModule.onInitialize();

        for (int count = 0; 10 > count; count++) {
            permissionIds.add(permissionsModule.addPermission(PERMISSION_PREFIX + count));
        }
    }

    private Rank createRank() {
        final String rankName = getRankName();
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

        assertThat(rank.getPermissions().toArray(new Integer[0])).isEmpty();
        for (final int permission : permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }
        assertThat(rank.getPermissions().toArray(new Integer[0]))
                .hasSize(permissionIds.size())
                .containsAll(permissionIds);
    }

    @Test
    void addPermissionDuplicate() {
        final Rank rank = this.createRank();

        final int permissionId = permissionIds.toArray(new Integer[0])[0];
        assertThat(rank.addPermission(permissionId)).isTrue();
        assertThat(rank.addPermission(permissionId)).isFalse();
    }

    @Test
    void removePermission() {
        final Rank rank = this.createRank();

        assertThat(rank.getPermissions().toArray(new Integer[0])).isEmpty();
        for (final int permission : permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }

        for (final int permission : permissionIds) {
            assertThat(rank.removePermission(permission)).isTrue();
        }
        assertThat(rank.getPermissions().toArray(new Integer[0])).isEmpty();
    }

    @Test
    void addPermissionNotExisting() {
        final Rank rank = this.createRank();

        final int permissionId = permissionIds.toArray(new Integer[0])[0];
        assertThat(rank.removePermission(permissionId)).isFalse();
    }


    @Test
    void getAllPermissions() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();
        final Rank extendedRank2 = this.createRank();
        final Rank extendedRank3 = this.createRank();

        final Set<Integer> permissionIds = new HashSet<>();
        final int rank3permission = permissionsModule.addPermission(PERMISSION_PREFIX + extendedRank3.getName());
        extendedRank3.addPermission(rank3permission);
        permissionIds.add(rank3permission);

        extendedRank2.addExtendedRank(extendedRank3);
        final int rank2permission = permissionsModule.addPermission(PERMISSION_PREFIX + extendedRank2.getName());
        extendedRank2.addPermission(rank2permission);
        permissionIds.add(rank2permission);

        mainRank.addExtendedRank(extendedRank1);
        final int rank1permission = permissionsModule.addPermission(PERMISSION_PREFIX + extendedRank1.getName());
        extendedRank1.addPermission(rank1permission);
        permissionIds.add(rank1permission);

        mainRank.addExtendedRank(extendedRank2);
        final int mainRankPermission = permissionsModule.addPermission(PERMISSION_PREFIX + mainRank.getName());
        mainRank.addPermission(mainRankPermission);
        mainRank.addPermission(rank3permission);
        permissionIds.add(mainRankPermission);

        assertThat(mainRank.getAllPermissions().toArray(new Integer[0]))
                .hasSize(permissionIds.size())
                .containsAll(permissionIds);

        // Cache check
        assertThat(mainRank.getAllPermissions().toArray(new Integer[0]))
                .hasSize(permissionIds.size())
                .containsAll(permissionIds);
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
    void hasExtendedRankId() {
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
    void addExtendedRankId() {
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
    void removeExtendedRankId() {
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
        rank.setName(newName);

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

        assertThat(rank.retrieveAllPlayers().toArray(new UserDb[0]))
                .hasSize(userDbList.size())
                .containsAll(userDbList);
    }
}