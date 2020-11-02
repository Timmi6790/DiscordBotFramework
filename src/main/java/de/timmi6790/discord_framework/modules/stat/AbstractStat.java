package de.timmi6790.discord_framework.modules.stat;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.Data;

@Data
public class AbstractStat {
    private final String name;
    private final String internalName;
    private int databaseId;

    public AbstractStat(final String name, final String internalName) {
        this.name = name;
        this.internalName = internalName;
    }

    protected void increaseStat(final UserDb userDb) {
        this.increaseStat(userDb, 1);
    }

    protected void increaseStat(final UserDb userDb, final int value) {
        userDb.increaseStat(this, value);
    }
}
