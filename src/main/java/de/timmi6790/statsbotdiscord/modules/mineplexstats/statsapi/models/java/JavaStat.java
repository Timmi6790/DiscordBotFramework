package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import lombok.Data;

import java.util.*;

@Data
public class JavaStat {
    private final String name;
    private final String[] aliasNames;
    private final String prettyStat;
    private final String description;

    private final Map<String, JavaBoard> boards;
    private final Map<String, String> boardAlias = new HashMap<>();

    public JavaStat(final String name, final String[] aliasNames, final String prettyStat, final String description, final Map<String, JavaBoard> boards) {
        this.name = name;
        this.aliasNames = aliasNames;
        this.prettyStat = prettyStat;
        this.description = description;
        this.boards = boards;

        for (final JavaBoard board : boards.values()) {
            final String boardLower = board.getName().toLowerCase();
            for (final String alias : board.getAliasNames()) {
                this.boardAlias.put(alias, boardLower);
            }
        }
    }

    public List<String> getBoardNames() {
        final List<String> names = new ArrayList<>();
        for (final JavaBoard board : this.boards.values()) {
            names.add(board.getName());
        }
        names.sort(Comparator.naturalOrder());

        return names;
    }

    public Optional<JavaBoard> getBoard(String name) {
        name = this.boardAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.boards.get(name));
    }

    public List<JavaBoard> getSimilarBoard(final String name, final double similarity, final int limit) {
        final List<JavaBoard> similarBoards = new ArrayList<>();

        final String[] similarCommandNames = UtilitiesData.getSimilarityList(name, this.boards.keySet(), similarity).toArray(new String[0]);
        for (int index = 0; Math.min(limit, similarCommandNames.length) > index; index++) {
            similarBoards.add(this.boards.get(similarCommandNames[index]));
        }

        return similarBoards;
    }
}