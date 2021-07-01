package de.timmi6790.discord_framework.module.modules.stat;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.stat.repository.StatRepository;
import de.timmi6790.discord_framework.module.modules.stat.repository.postgres.StatPostgresRepository;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class StatModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractStat> stats = new HashMap<>();
    private final Map<String, Integer> nameIdMatching = new HashMap<>();

    private StatRepository statRepository;
    private EventModule eventModule;

    public StatModule() {
        super("StatModule");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                EventModule.class
        );
    }

    protected int getStatIdOrCreate(final String internalName) {
        return this.statRepository.getStatId(internalName)
                .orElseGet(() -> this.statRepository.createStat(internalName));
    }

    @Override
    public boolean onInitialize() {
        this.statRepository = new StatPostgresRepository(
                this.getModuleOrThrow(DatabaseModule.class).getJdbi()
        );
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
        stat.setDatabaseId(this.getStatIdOrCreate(stat.getInternalName()));

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
