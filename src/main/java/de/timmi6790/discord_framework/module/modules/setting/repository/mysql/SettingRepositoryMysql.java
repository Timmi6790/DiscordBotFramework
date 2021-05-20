package de.timmi6790.discord_framework.module.modules.setting.repository.mysql;

import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.setting.repository.SettingRepository;

import java.util.Map;

public class SettingRepositoryMysql implements SettingRepository {
    private static final String SETTING_NAME = "settingName";

    private static final String GET_SETTING_ID = "SELECT id FROM setting WHERE setting_name = :settingName LIMIT 1;";
    private static final String INSERT_SETTING = "INSERT INTO setting(setting_name) VALUES(:settingName);";

    private final DatabaseModule databaseModule;

    public SettingRepositoryMysql(final DatabaseModule databaseModule) {
        this.databaseModule = databaseModule;
    }

    @Override
    public int retrieveOrCreateSettingId(final String internalName) {
        return this.databaseModule.retrieveOrCreateId(
                GET_SETTING_ID,
                Map.of(SETTING_NAME, internalName),
                INSERT_SETTING,
                Map.of(SETTING_NAME, internalName)
        );
    }
}
