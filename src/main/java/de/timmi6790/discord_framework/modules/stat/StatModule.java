package de.timmi6790.discord_framework.modules.stat;


import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.stat.repository.StatRepository;
import de.timmi6790.discord_framework.modules.stat.repository.mysql.StatRepositoryMysql;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode
public class StatModule implements Module {
    @Getter
    private final Map<Integer, AbstractStat> stats = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    private final StatRepository statRepository;
    private final EventModule eventModule;

    public StatModule(final StatRepositoryMysql statRepository, final EventModule eventModule) {
        this.statRepository = statRepository;

        this.eventModule = eventModule;
    }

    @Override
    public String getName() {
        return "StatModule";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
    }

    public boolean hasStat(final AbstractStat stat) {
        return this.getStat(stat.getDatabaseId()).isPresent();
    }

    public void registerStats(@NonNull final Module module, final AbstractStat... stats) {
        for (final AbstractStat stat : stats) {
            this.registerStat(module, stat);
        }
    }

    public boolean registerStat(@NonNull final Module module, @NonNull final AbstractStat stat) {
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
