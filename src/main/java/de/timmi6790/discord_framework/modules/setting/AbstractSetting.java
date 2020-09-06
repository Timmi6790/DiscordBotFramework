package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import lombok.Data;

@Data
public abstract class AbstractSetting<T> {
    private static final String SETTING_NAME = "settingName";

    private static final String GET_SETTING_ID = "SELECT id FROM setting WHERE setting_name = :settingName LIMIT 1;";
    private static final String INSERT_SETTING = "INSERT INTO setting(setting_name) VALUES(:settingName);";

    private final int databaseId;
    private final String internalName;
    private final String name;
    private final String defaultValue;

    public AbstractSetting(final String internalName, final String name, final String defaultValue) {
        this.name = name;
        this.internalName = internalName;
        this.defaultValue = defaultValue;
        this.databaseId = this.getSettingDbId();
    }

    private int getSettingDbId() {
        // Get current id or insert new
        return DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().withHandle(handle ->
                handle.createQuery(GET_SETTING_ID)
                        .bind(SETTING_NAME, this.getInternalName())
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(INSERT_SETTING)
                                    .bind(SETTING_NAME, this.getInternalName())
                                    .execute();

                            return handle.createQuery(GET_SETTING_ID)
                                    .bind(SETTING_NAME, this.getInternalName())
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }

    public abstract boolean isAllowedValue(String value);

    public abstract String toDatabaseValue(T value);

    public abstract T fromDatabaseValue(String value);
}
