package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.utilities.StatsComparator;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;

@Data
public class JavaGame {
    private static final Pattern ILLEGAL_STAT_CHARACTERS = Pattern.compile("([ !<,\\.?`'])|(Achievement)", Pattern.CASE_INSENSITIVE);

    private final String name;
    private final String[] aliasNames;
    private final String category;
    private final String wikiUrl;
    private final String description;

    private final Map<String, JavaStat> stats;
    private final Map<String, String> statAlias = new HashMap<>();
    private final List<String> sortedStatsNames = new ArrayList<>();

    public JavaGame(final String name, final String[] aliasNames, final String category, final String wikiUrl, final String description, final Map<String, JavaStat> stats) {
        this.name = name;
        this.aliasNames = aliasNames;
        this.category = category;
        this.wikiUrl = wikiUrl;
        this.description = description;
        this.stats = stats;

        for (final JavaStat gameStat : stats.values()) {
            final String cleanStat = getCleanStat(gameStat.getName()).toLowerCase();
            for (final String alias : gameStat.getAliasNames()) {
                this.statAlias.put(alias.toLowerCase(), cleanStat);
            }
        }
    }

    public static String getCleanStat(final String name) {
        return ILLEGAL_STAT_CHARACTERS.matcher(name).replaceAll("");
    }

    public Optional<JavaStat> getStat(String name) {
        name = JavaGame.getCleanStat(name);
        name = this.statAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.stats.get(name));
    }

    public List<String> getStatNames() {
        if (this.sortedStatsNames.isEmpty()) {
            final List<JavaStat> stats = new ArrayList<>(this.stats.values());
            stats.sort(new StatsComparator());

            for (final JavaStat stat : stats) {
                this.sortedStatsNames.add(stat.getPrintName());
            }
        }

        return this.sortedStatsNames;
    }

    public List<JavaStat> getSimilarStats(final String name, final double similarity, final int limit) {
        final List<JavaStat> similarGames = new ArrayList<>();

        final String[] similarCommandNames = UtilitiesData.getSimilarityList(name, this.stats.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarGames.add(this.stats.get(similarCommandNames[index]));
        }

        return similarGames;
    }
}