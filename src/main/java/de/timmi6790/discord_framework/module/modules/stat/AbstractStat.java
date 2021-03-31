package de.timmi6790.discord_framework.module.modules.stat;

import de.timmi6790.discord_framework.module.modules.user.UserDb;
import lombok.Data;

@Data
public class AbstractStat {
    private final String name;
    private String internalName;
    private int databaseId;

    public AbstractStat(final String name) {
        this.name = name;
    }

    protected void increaseStat(final UserDb userDb) {
        this.increaseStat(userDb, 1);
    }

    protected void increaseStat(final UserDb userDb, final int value) {
        userDb.increaseStat(this, value);
    }
}
