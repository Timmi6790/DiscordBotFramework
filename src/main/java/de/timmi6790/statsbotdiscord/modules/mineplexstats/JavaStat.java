package de.timmi6790.statsbotdiscord.modules.mineplexstats;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class JavaStat {
    private final String name;
    private final String[] aliasNames;
    private final String prettyStat;
    private final String description;

    private final Map<String, String> boards;
    private final Map<String, String> boardAlias;

    public List<String> getBoardNames() {
        return new ArrayList<>(this.boards.values());
    }
}