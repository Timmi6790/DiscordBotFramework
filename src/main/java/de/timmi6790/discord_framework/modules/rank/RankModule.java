package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.rank.commands.RankCommand;
import de.timmi6790.discord_framework.modules.rank.repository.RankRepository;
import de.timmi6790.discord_framework.modules.rank.repository.RankRepositoryMysql;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Getter
public class RankModule extends AbstractModule {
    private final ConcurrentHashMap<Integer, Rank> rankMap = new ConcurrentHashMap<>();
    private final Map<Integer, String> rankMappingMap = new ConcurrentHashMap<>();

    private RankRepository rankRepository;

    public RankModule() {
        super("Rank");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                UserDbModule.class,
                CommandModule.class
        );
    }

    private void loadRanks() {
        for (final Rank rank : this.getRankRepository().loadRanks()) {
            this.addRank(rank);
        }
    }

    private void addRank(@NonNull final Rank rank) {
        this.getRankMappingMap().put(rank.getDatabaseId(), rank.getName());
        this.getRankMap().put(rank.getDatabaseId(), rank);
        this.invalidateAllPermCaches();
    }

    @Override
    public void onInitialize() {
        this.rankRepository = new RankRepositoryMysql(this);
        this.loadRanks();

        this.getModuleOrThrow(CommandModule.class)
                .registerCommands(
                        this,
                        new RankCommand()
                );
    }

    public void invalidateAllPermCaches() {
        this.rankMap.values().forEach(Rank::invalidateCachedPermissions);
    }

    public boolean hasRank(final int id) {
        return this.rankMap.containsKey(id);
    }

    public boolean hasRank(@NonNull final String name) {
        return this.rankMappingMap.containsValue(name);
    }

    public Optional<Rank> getRank(final int id) {
        return Optional.ofNullable(this.rankMap.get(id));
    }

    public Optional<Rank> getRank(@NonNull final String name) {
        for (final Map.Entry<Integer, String> entry : this.getRankMappingMap().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return this.getRank(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public Set<Rank> getRanks() {
        return new HashSet<>(this.getRankMap().values());
    }

    public boolean createRank(@NonNull final String name) {
        if (this.hasRank(name)) {
            return false;
        }

        final Rank newRank = this.getRankRepository().createRank(name);
        this.addRank(newRank);

        return true;
    }

    public boolean deleteRank(@NonNull final Rank rank) {
        return this.deleteRank(rank.getDatabaseId());
    }

    public boolean deleteRank(final int rankId) {
        // Never allow anyone to delete the default rank with id 1
        if (!this.hasRank(rankId) || rankId == 1) {
            return false;
        }

        this.getRankRepository().deleteRank(rankId);
        this.getRankMap().remove(rankId);
        this.getRankMappingMap().remove(rankId);

        this.invalidateAllPermCaches();

        return true;
    }
}
