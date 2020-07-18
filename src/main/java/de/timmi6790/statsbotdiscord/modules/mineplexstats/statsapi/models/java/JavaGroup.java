package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.utilities.StatsComparator;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class JavaGroup {
    private final String group;
    private final String description;
    private final String[] aliasNames;
    private final List<String> games;
    private List<JavaStat> groupStats;

    public String getName() {
        return this.group;
    }

    public List<JavaGame> getGames() {
        final MineplexStatsModule module = StatsBot.getModuleManager().getModule(MineplexStatsModule.class).orElseThrow(RuntimeException::new);
        return this.games.stream()
                .map(module::getJavaGame)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<JavaGame> getGames(final JavaStat stat) {
        final MineplexStatsModule module = StatsBot.getModuleManager().getModule(MineplexStatsModule.class).orElseThrow(RuntimeException::new);
        return this.games.stream()
                .map(module::getJavaGame)
                .filter(gameOpt -> gameOpt.map(game -> game.getStat(stat.getName()).isPresent()).orElse(false))
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<String> getGameNames() {
        return this.games;
    }

    public List<JavaStat> getStats() {
        if (this.groupStats == null) {
            this.groupStats = this.getGames()
                    .stream()
                    .flatMap(game -> game.getStats().values().stream())
                    .sorted(new StatsComparator())
                    .distinct()
                    .collect(Collectors.toList());
        }

        return this.groupStats;
    }

    public Set<String> getStatNames() {
        return this.getStats()
                .stream()
                .map(JavaStat::getPrintName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
