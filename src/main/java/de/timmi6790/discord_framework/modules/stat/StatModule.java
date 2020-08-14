package de.timmi6790.discord_framework.modules.stat;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
public class StatModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractStat> stats = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public StatModule() {
        super("StatModule");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                EventModule.class
        );
    }

    @Override
    public void onInitialize() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public boolean hasStat(final AbstractStat stat) {
        return this.stats.containsKey(stat.getDatabaseId());
    }

    public boolean registerStat(final AbstractStat stat) {
        if (this.hasStat(stat)) {
            return false;
        }

        this.stats.put(stat.getDatabaseId(), stat);
        this.nameIdMatching.put(stat.getInternalName(), stat.getDatabaseId());

        this.getModuleOrThrow(EventModule.class).addEventListener(stat);
        return true;
    }

    public void registerStats(final AbstractStat... stats) {
        Arrays.stream(stats).forEach(this::registerStat);
    }

    public void increaseStat(final UserDb userDb, final AbstractStat stat, final int value) {
        userDb.increaseStat(stat, value);
    }
}
