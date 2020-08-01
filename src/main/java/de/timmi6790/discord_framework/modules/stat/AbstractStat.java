package de.timmi6790.discord_framework.modules.stat;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.Data;

@Data
public class AbstractStat {
    private static final String GET_STAT_ID = "SELECT id FROM `stat` WHERE stat_name = :statName LIMIT 1;";
    private static final String INSERT_NEW_STAT = "INSERT INTO stat(stat_name) VALUES(:statName);";

    private final int databaseId;
    private final String name;
    private final String internalName;

    public AbstractStat(final String name, final String internalName) {
        this.name = name;
        this.internalName = internalName;
        this.databaseId = this.getStatDbId();
    }

    private int getStatDbId() {
        return DiscordBot.getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().withHandle(handle ->
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
