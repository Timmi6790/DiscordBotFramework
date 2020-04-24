package de.timmi6790.statsbotdiscord.modules.stat;

import lombok.Data;

@Data
public class AbstractStat {
    private int dbId = 0;
    private final String internalName;
    private final String name;
}
