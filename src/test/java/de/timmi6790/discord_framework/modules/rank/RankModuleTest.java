package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RankModuleTest {
    private static final AtomicInteger RANK_NAME_NUMBER = new AtomicInteger(0);

    @Spy
    private static final RankModule rankModule = Mockito.spy(new RankModule());
    @Spy
    private static final PermissionsModule permissionsModule = Mockito.spy(new PermissionsModule());
    @Spy
    private static final UserDbModule userDbModule = Mockito.spy(new UserDbModule());

    private static String getRankName() {
        return "RandomRank" + RANK_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {
        doReturn(AbstractIntegrationTest.databaseModule).when(permissionsModule).getModuleOrThrow(DatabaseModule.class);
        permissionsModule.onInitialize();

        final CommandModule commandModule = spy(new CommandModule());
        doNothing().when(commandModule).registerCommands(any(), any());

        doReturn(AbstractIntegrationTest.databaseModule).when(userDbModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(commandModule).when(userDbModule).getModuleOrThrow(CommandModule.class);
        userDbModule.onInitialize();

        doReturn(AbstractIntegrationTest.databaseModule).when(rankModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(userDbModule).when(rankModule).getModuleOrThrow(UserDbModule.class);
        doReturn(commandModule).when(rankModule).getModuleOrThrow(CommandModule.class);

        rankModule.onInitialize();
    }

    @Test
    void hasRank() {
        final String rankName = getRankName();
        rankModule.createRank(rankName);

        final Optional<Rank> rankByName = rankModule.getRank(rankName);
        assertThat(rankByName).isPresent();

        assertThat(rankModule.hasRank(rankByName.get().getDatabaseId())).isTrue();
    }

    @Test
    void getRankId() {
        final String rankName = getRankName();
        rankModule.createRank(rankName);

        final Optional<Rank> rankByName = rankModule.getRank(rankName);
        assertThat(rankByName).isPresent();

        final Optional<Rank> rankById = rankModule.getRank(rankByName.get().getDatabaseId());
        assertThat(rankById)
                .isPresent()
                .hasValue(rankByName.get());
    }

    @Test
    void getRankByName() {
        final String rankName = getRankName();
        rankModule.createRank(rankName);

        final Optional<Rank> rank = rankModule.getRank(rankName);
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
            rankModule.createRank(rankName);
        }

        final List<String> foundRankNames = rankModule.getRanks().stream()
                .map(Rank::getName)
                .collect(Collectors.toList());
        assertThat(foundRankNames).containsAll(rankNames);
    }

    @Test
    void createRank() {
        final String rankName = getRankName();
        assertThat(rankModule.hasRank(rankName)).isFalse();

        final boolean rankCreated = rankModule.createRank(rankName);
        assertThat(rankCreated).isTrue();

        final boolean alreadyCreated = rankModule.createRank(rankName);
        assertThat(alreadyCreated).isFalse();

        final Optional<Rank> rank = rankModule.getRank(rankName);
        assertThat(rank).isPresent();
        assertThat(rank.get().getName()).isEqualTo(rankName);
    }

    @Test
    void deleteRankRank() {
        final String rankName = getRankName();
        rankModule.createRank(rankName);
        final Optional<Rank> rank = rankModule.getRank(rankName);

        assertThat(rank).isPresent();
        assertThat(rankModule.deleteRank(rank.get())).isTrue();
        assertThat(rankModule.hasRank(rankName)).isFalse();
    }

    @Test
    void deleteRankId() {
        final String rankName = getRankName();
        rankModule.createRank(rankName);
        final Optional<Rank> rank = rankModule.getRank(rankName);

        assertThat(rank).isPresent();
        assertThat(rankModule.deleteRank(rank.get().getDatabaseId())).isTrue();
        assertThat(rankModule.hasRank(rankName)).isFalse();
    }

    @Test
    void deleteDefaultRank() {
        assertThat(rankModule.deleteRank(1)).isFalse();
    }
}