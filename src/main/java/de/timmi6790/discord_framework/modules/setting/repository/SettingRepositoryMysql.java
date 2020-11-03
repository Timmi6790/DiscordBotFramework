package de.timmi6790.discord_framework.modules.setting.repository;

import de.timmi6790.commons.builders.MapBuilder;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.setting.SettingModule;

public class SettingRepositoryMysql implements SettingRepository {
    private static final String SETTING_NAME = "settingName";

    private static final String GET_SETTING_ID = "SELECT id FROM setting WHERE setting_name = :settingName LIMIT 1;";
    private static final String INSERT_SETTING = "INSERT INTO setting(setting_name) VALUES(:settingName);";

    private final DatabaseModule databaseModule;

    public SettingRepositoryMysql(final SettingModule module) {
        this.databaseModule = module.getModuleOrThrow(DatabaseModule.class);
    }

    @Override
    public int retrieveOrCreateSettingId(final String internalName) {
        return this.databaseModule.retrieveOrCreateId(
                GET_SETTING_ID,
                MapBuilder.<String, Object>ofHashMap()
                        .put(SETTING_NAME, internalName)
                        .build(),
                INSERT_SETTING,
                MapBuilder.<String, Object>ofHashMap()
                        .put(SETTING_NAME, internalName)
                        .build()
        );
    }
}
