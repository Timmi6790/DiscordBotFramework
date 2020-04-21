package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.utilities.StatsComparator;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class JavaGroup {
    private final String group;
    private final String description;
    private final String[] aliasNames;
    private final List<String> games;

    public String getName() {
        return this.group;
    }

    public List<JavaGame> getGames() {
        final MineplexStatsModule module = (MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class);

        final List<JavaGame> games = new ArrayList<>();
        for (final String name : this.games) {
            module.getJavaGame(name).ifPresent(games::add);
        }

        return games;
    }

    public List<JavaGame> getGames(final JavaStat stat) {
        final MineplexStatsModule module = (MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class);

        final List<JavaGame> games = new ArrayList<>();
        for (final String name : this.games) {
            module.getJavaGame(name).ifPresent(game -> {
                game.getStat(stat.getName()).ifPresent(stat1 -> games.add(game));
            });
        }

        return games;
    }

    public List<String> getGameNames() {
        return this.games;
    }

    public List<JavaStat> getStats() {
        // TODO: Think about making a save after running it once, it really depends on the use
        final Set<JavaStat> stats = new HashSet<>();
        for (final JavaGame game : this.getGames()) {
            stats.addAll(game.getStats().values());
        }

        final List<JavaStat> statsList = new ArrayList<>(stats);
        statsList.sort(new StatsComparator());
        return statsList;
    }

    public List<String> getStatNames() {
        final List<String> stats = new ArrayList<>();
        for (final JavaStat stat : this.getStats()) {
            stats.add(stat.getName());
        }

        return stats;
    }
}
