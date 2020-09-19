package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.fake_modules.FakeEmptyCommandModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static de.timmi6790.discord_framework.AbstractIntegrationTest.MARIA_DB_CONTAINER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RankTest {
    private static final String PERMISSION_PREFIX = "rank_test.";
    private static final AtomicInteger RANK_NAME_NUMBER = new AtomicInteger(0);

    @Spy
    private final RankModule rankModule = new RankModule();
    @Spy
    private final PermissionsModule permissionsModule = new PermissionsModule();
    @Spy
    private final UserDbModule userDbModule = new UserDbModule();

    private final Set<Integer> permissionIds = new HashSet<>();

    private static String getRankName() {
        return "RandomRankRank" + RANK_NAME_NUMBER.getAndIncrement();
    }

    private Rank createRank() {
        final String rankName = getRankName();
        this.rankModule.createRank(rankName);

        return this.rankModule.getRank(rankName).orElseThrow(RuntimeException::new);
    }

    @BeforeEach
    void setup() {
        final FakeDatabaseModel fakeDatabaseModel = new FakeDatabaseModel(MARIA_DB_CONTAINER);

        doReturn(fakeDatabaseModel).when(this.permissionsModule).getModuleOrThrow(DatabaseModule.class);
        this.permissionsModule.onInitialize();

        doReturn(fakeDatabaseModel).when(this.userDbModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(new FakeEmptyCommandModule()).when(this.userDbModule).getModuleOrThrow(CommandModule.class);
        this.userDbModule.onInitialize();

        doReturn(fakeDatabaseModel).when(this.rankModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(this.userDbModule).when(this.rankModule).getModuleOrThrow(UserDbModule.class);
        doReturn(new FakeEmptyCommandModule()).when(this.rankModule).getModuleOrThrow(CommandModule.class);

        this.rankModule.onInitialize();

        for (int count = 0; 10 > count; count++) {
            this.permissionIds.add(this.permissionsModule.addPermission(PERMISSION_PREFIX + count));
        }
    }

    @Test
    void hasPermission() {
        final Rank rank = this.createRank();

        for (final int permission : this.permissionIds) {
            assertThat(rank.hasPermission(permission)).isFalse();
        }

        for (final int permission : this.permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }

        for (final int permission : this.permissionIds) {
            assertThat(rank.hasPermission(permission)).isTrue();
        }
    }

    @Test
    void addPermission() {
        final Rank rank = this.createRank();

        assertThat(rank.getPermissions().toArray(new Integer[0])).isEmpty();
        for (final int permission : this.permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }
        assertThat(rank.getPermissions().toArray(new Integer[0]))
                .hasSize(this.permissionIds.size())
                .containsAll(this.permissionIds);
    }

    @Test
    void removePermission() {
        final Rank rank = this.createRank();

        assertThat(rank.getPermissions().toArray(new Integer[0])).isEmpty();
        for (final int permission : this.permissionIds) {
            assertThat(rank.addPermission(permission)).isTrue();
        }

        for (final int permission : this.permissionIds) {
            assertThat(rank.removePermission(permission)).isTrue();
        }
        assertThat(rank.getPermissions().toArray(new Integer[0])).isEmpty();
    }

    @Test
    void getAllPermissions() {
        final Rank mainRank = this.createRank();
        final Rank extendedRank1 = this.createRank();
        final Rank extendedRank2 = this.createRank();
        final Rank extendedRank3 = this.createRank();

        final Set<Integer> permissionIds = new HashSet<>();
        final int rank3permission = this.permissionsModule.addPermission(PERMISSION_PREFIX + extendedRank3.getName());
        extendedRank3.addPermission(rank3permission);
        permissionIds.add(rank3permission);

        extendedRank2.addExtendedRank(extendedRank3);
        final int rank2permission = this.permissionsModule.addPermission(PERMISSION_PREFIX + extendedRank2.getName());
        extendedRank2.addPermission(rank2permission);
        permissionIds.add(rank2permission);

        mainRank.addExtendedRank(extendedRank1);
        final int rank1permission = this.permissionsModule.addPermission(PERMISSION_PREFIX + extendedRank1.getName());
        extendedRank1.addPermission(rank1permission);
        permissionIds.add(rank1permission);

        mainRank.addExtendedRank(extendedRank2);
        final int mainRankPermission = this.permissionsModule.addPermission(PERMISSION_PREFIX + mainRank.getName());
        mainRank.addPermission(mainRankPermission);
        permissionIds.add(mainRankPermission);

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
        final UserDb user1 = this.userDbModule.getOrCreate(1);
        final UserDb user2 = this.userDbModule.getOrCreate(2);

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
            final UserDb userDb = this.userDbModule.getOrCreate(count);
            userDb.setPrimaryRank(rank);
            userDbList.add(userDb);
        }

        assertThat(rank.retrieveAllPlayers().toArray(new UserDb[0]))
                .hasSize(userDbList.size())
                .containsAll(userDbList);
    }
}