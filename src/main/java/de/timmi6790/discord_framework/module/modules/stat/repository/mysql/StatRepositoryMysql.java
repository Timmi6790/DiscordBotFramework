package de.timmi6790.discord_framework.module.modules.stat.repository.mysql;

import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.stat.repository.StatRepository;

import java.util.Map;

public class StatRepositoryMysql implements StatRepository {
    private static final String STAT_NAME = "statName";

    private static final String GET_STAT_ID = "SELECT id FROM `stat` WHERE stat_name = :statName LIMIT 1;";
    private static final String INSERT_NEW_STAT = "INSERT INTO stat(stat_name) VALUES(:statName);";

    private final DatabaseModule databaseModule;

    public StatRepositoryMysql(final DatabaseModule databaseModule) {
        this.databaseModule = databaseModule;
    }

    @Override
    public int retrieveOrCreateSettingId(final String internalName) {
        return this.databaseModule.retrieveOrCreateId(
                GET_STAT_ID,
                Map.of(STAT_NAME, internalName),
                INSERT_NEW_STAT,
                Map.of(STAT_NAME, internalName)
        );
    }
}
