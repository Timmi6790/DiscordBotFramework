package de.timmi6790.discord_framework.modules.stat;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.stat.repository.StatRepository;
import de.timmi6790.discord_framework.modules.stat.repository.mysql.StatRepositoryMysql;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
public class StatModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractStat> stats = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    private StatRepository statRepository;
    private EventModule eventModule;

    public StatModule() {
        super("StatModule");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                EventModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.statRepository = new StatRepositoryMysql(this.getModuleOrThrow(DatabaseModule.class));
        this.eventModule = this.getModuleOrThrow(EventModule.class);

        return true;
    }

    public boolean hasStat(final AbstractStat stat) {
        return this.getStat(stat.getDatabaseId()).isPresent();
    }

    public void registerStats(@NonNull final AbstractModule module, final AbstractStat... stats) {
        for (final AbstractStat stat : stats) {
            this.registerStat(module, stat);
        }
    }

    public boolean registerStat(@NonNull final AbstractModule module, @NonNull final AbstractStat stat) {
        if (this.hasStat(stat)) {
            return false;
        }

        stat.setInternalName(this.generateInternalName(module, "stat", stat.getName()));
        stat.setDatabaseId(this.statRepository.retrieveOrCreateSettingId(stat.getInternalName()));

        this.stats.put(stat.getDatabaseId(), stat);
        this.nameIdMatching.put(stat.getName(), stat.getDatabaseId());

        this.eventModule.addEventListener(stat);
        return true;
    }

    public Optional<AbstractStat> getStat(@NonNull final String statName) {
        final Integer statId = this.nameIdMatching.get(statName);
        if (statId != null) {
            return this.getStat(statId);
        }

        return Optional.empty();
    }

    public Optional<AbstractStat> getStat(final int statId) {
        return Optional.ofNullable(this.stats.get(statId));
    }
}
