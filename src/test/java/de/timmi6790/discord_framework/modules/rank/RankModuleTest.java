package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.fake_modules.FakeEmptyCommandModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class RankModuleTest {
    private static final AtomicInteger RANK_NAME_NUMBER = new AtomicInteger(0);

    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = (MariaDBContainer) new MariaDBContainer().withClasspathResourceMapping(
            "tables.sql",
            "/docker-entrypoint-initdb.d/createTables.sql",
            BindMode.READ_ONLY
    );

    @Spy
    private final RankModule rankModule = new RankModule();
    @Spy
    private final PermissionsModule permissionsModule = new PermissionsModule();
    @Spy
    private final UserDbModule userDbModule = new UserDbModule();

    private static String getRankName() {
        return "RandomRank" + RANK_NAME_NUMBER.getAndIncrement();
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
    }

    @Test
    void hasRank() {
        final String rankName = getRankName();
        this.rankModule.createRank(rankName);

        final Optional<Rank> rankByName = this.rankModule.getRank(rankName);
        assertThat(rankByName).isPresent();

        assertThat(this.rankModule.hasRank(rankByName.get().getDatabaseId())).isTrue();
    }

    @Test
    void getRankId() {
        final String rankName = getRankName();
        this.rankModule.createRank(rankName);

        final Optional<Rank> rankByName = this.rankModule.getRank(rankName);
        assertThat(rankByName).isPresent();

        final Optional<Rank> rankById = this.rankModule.getRank(rankByName.get().getDatabaseId());
        assertThat(rankById)
                .isPresent()
                .hasValue(rankByName.get());
    }

    @Test
    void getRankByName() {
        final String rankName = getRankName();
        this.rankModule.createRank(rankName);

        final Optional<Rank> rank = this.rankModule.getRank(rankName);
        assertThat(rank).isPresent();
        assertThat(rank.get().getName()).isEqualTo(rankName);
    }

    @Test
    void getRanks() {
        final List<String> rankNames = new ArrayList<>();
        for (int count = 0; 10 > count; count++) {
            rankNames.add(getRankName());
        }

        for (final String rankName : rankNames) {
            this.rankModule.createRank(rankName);
        }

        final List<String> foundRankNames = this.rankModule.getRanks().stream()
                .map(Rank::getName)
                .collect(Collectors.toList());
        assertThat(foundRankNames).containsAll(rankNames);
    }

    @Test
    void createRank() {
        final String rankName = getRankName();
        assertThat(this.rankModule.hasRank(rankName)).isFalse();

        final boolean rankCreated = this.rankModule.createRank(rankName);
        assertThat(rankCreated).isTrue();

        final boolean alreadyCreated = this.rankModule.createRank(rankName);
        assertThat(alreadyCreated).isFalse();

        final Optional<Rank> rank = this.rankModule.getRank(rankName);
        assertThat(rank).isPresent();
        assertThat(rank.get().getName()).isEqualTo(rankName);
    }

    @Test
    void deleteRankRank() {
        final String rankName = getRankName();
        this.rankModule.createRank(rankName);
        final Optional<Rank> rank = this.rankModule.getRank(rankName);

        assertThat(rank).isPresent();
        assertThat(this.rankModule.deleteRank(rank.get())).isTrue();
        assertThat(this.rankModule.hasRank(rankName)).isFalse();
    }

    @Test
    void deleteRankId() {
        final String rankName = getRankName();
        this.rankModule.createRank(rankName);
        final Optional<Rank> rank = this.rankModule.getRank(rankName);

        assertThat(rank).isPresent();
        assertThat(this.rankModule.deleteRank(rank.get().getDatabaseId())).isTrue();
        assertThat(this.rankModule.hasRank(rankName)).isFalse();
    }
}