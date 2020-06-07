package de.timmi6790.statsbotdiscord.modules.stat;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import lombok.Data;

import java.util.regex.Pattern;

@Data
public class AbstractStat {
    private final static Pattern INTERNAL_NAME_REPLACE_PATTERN = Pattern.compile("([ !<,\\.?`'])");

    private final static String GET_STAT_ID = "SELECT id FROM `stat` WHERE stat_name = :statName LIMIT 1;";
    private final static String INSERT_NEW_STAT = "INSERT INTO stat(stat_name) VALUES(:statName);";

    private final int databaseId;
    private final String name;
    private final String internalName;

    public AbstractStat(final String name, final String internalName) {
        this.name = name;
        this.internalName = internalName;
        this.databaseId = this.getStatDbId();
    }

    private int getStatDbId() {
        return StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery(GET_STAT_ID)
                        .bind("statName", this.getInternalName())
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(INSERT_NEW_STAT)
                                    .bind("statName", this.getInternalName())
                                    .execute();

                            return handle.createQuery(GET_STAT_ID)
                                    .bind("statName", this.getInternalName())
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }

    protected void increaseStat(final UserDb userDb) {
        this.increaseStat(userDb, 1);
    }

    protected void increaseStat(final UserDb userDb, final int value) {
        userDb.increaseStat(this, value);
    }
}
