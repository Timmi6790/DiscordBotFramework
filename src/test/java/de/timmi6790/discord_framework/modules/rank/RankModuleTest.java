package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

class RankModuleTest {
    private static final AtomicInteger RANK_NAME_NUMBER = new AtomicInteger(0);

    private static final RankModule rankModule = spy(RankModule.class);
    private static final PermissionsModule permissionsModule = spy(PermissionsModule.class);
    private static final UserDbModule userDbModule = spy(UserDbModule.class);

    private static String generateRankName() {
        return "RandomRank" + RANK_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {
       
    }

    @Test
    void hasRank() {
        final String rankName = generateRankName();
        rankModule.createRank(rankName);

        final Optional<Rank> rankByName = rankModule.getRank(rankName);
        assertThat(rankByName).isPresent();

        assertThat(rankModule.hasRank(rankByName.get().getRepositoryId())).isTrue();
    }

    @Test
    void getRankId() {
        final String rankName = generateRankName();
        rankModule.createRank(rankName);

        final Optional<Rank> rankByName = rankModule.getRank(rankName);
        assertThat(rankByName).isPresent();

        final Optional<Rank> rankById = rankModule.getRank(rankByName.get().getRepositoryId());
        assertThat(rankById)
                .isPresent()
                .hasValue(rankByName.get());
    }

    @Test
    void getRankByName() {
        final String rankName = generateRankName();
        rankModule.createRank(rankName);

        final Optional<Rank> rank = rankModule.getRank(rankName);
        assertThat(rank).isPresent();
        assertThat(rank.get().getRankName()).isEqualTo(rankName);
    }

    @Test
    void getRanks() {
        final List<String> rankNames = new ArrayList<>();
        for (int count = 0; 10 > count; count++) {
            rankNames.add(generateRankName());
        }

        for (final String rankName : rankNames) {
            rankModule.createRank(rankName);
        }

        final List<String> foundRankNames = DataUtilities.convertToStringList(rankModule.getRanks(), Rank::getRankName);
        assertThat(foundRankNames).containsAll(rankNames);
    }

    @Test
    void createRank() {
        final String rankName = generateRankName();
        assertThat(rankModule.hasRank(rankName)).isFalse();

        final boolean rankCreated = rankModule.createRank(rankName);
        assertThat(rankCreated).isTrue();

        final boolean alreadyCreated = rankModule.createRank(rankName);
        assertThat(alreadyCreated).isFalse();

        final Optional<Rank> rank = rankModule.getRank(rankName);
        assertThat(rank).isPresent();
        assertThat(rank.get().getRankName()).isEqualTo(rankName);
    }

    @Test
    void deleteRankRank() {
        final String rankName = generateRankName();
        rankModule.createRank(rankName);
        final Optional<Rank> rank = rankModule.getRank(rankName);

        assertThat(rank).isPresent();
        assertThat(rankModule.deleteRank(rank.get())).isTrue();
        assertThat(rankModule.hasRank(rankName)).isFalse();
    }

    @Test
    void deleteRankId() {
        final String rankName = generateRankName();
        rankModule.createRank(rankName);
        final Optional<Rank> rank = rankModule.getRank(rankName);

        assertThat(rank).isPresent();
        assertThat(rankModule.deleteRank(rank.get().getRepositoryId())).isTrue();
        assertThat(rankModule.hasRank(rankName)).isFalse();
    }

    @Test
    void deleteDefaultRank() {
        assertThat(rankModule.deleteRank(1)).isFalse();
    }

    @Test
    void delete_missing_rank() {
        assertThat(rankModule.deleteRank(-1)).isFalse();
    }

    @Test
    void getRank_none_existing() {
        assertThat(rankModule.getRank(generateRankName())).isEmpty();
    }
}