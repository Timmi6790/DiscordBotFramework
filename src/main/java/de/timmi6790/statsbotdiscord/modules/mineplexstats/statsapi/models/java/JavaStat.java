package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class JavaStat {
    private final String name;
    private final String[] aliasNames;
    private final boolean achievement;
    private final String description;

    private final Map<String, JavaBoard> boards;
    private final Map<String, String> boardAlias = new HashMap<>();

    public JavaStat(final String name, final String[] aliasNames, final boolean achievement, final String description, final Map<String, JavaBoard> boards) {
        this.name = name;
        this.aliasNames = aliasNames;
        this.achievement = achievement;
        this.description = description;
        this.boards = boards;

        boards.values().forEach(board -> {
            final String boardLower = board.getName().toLowerCase();
            Arrays.stream(board.getAliasNames()).forEach(alias -> this.boardAlias.put(alias, boardLower));
        });
    }

    public String getPrintName() {
        if (this.achievement) {
            return "Achievement " + this.name;
        }

        return this.name;
    }

    public List<String> getBoardNames() {
        return this.boards.values()
                .stream()
                .map(JavaBoard::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    public Optional<JavaBoard> getBoard(String name) {
        name = this.boardAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.boards.get(name));
    }

    public List<JavaBoard> getSimilarBoard(final String name, final double similarity, final int limit) {
        return UtilitiesData.getSimilarityList(name, this.boards.keySet(), similarity, limit)
                .stream()
                .map(this.boards::get)
                .collect(Collectors.toList());
    }
}