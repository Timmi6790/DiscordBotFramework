package de.timmi6790.statsbotdiscord.modules.setting;

import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.Data;

@Data
public abstract class AbstractSetting<T> {
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
        return StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery(GET_SETTING_ID)
                        .bind("settingName", this.getInternalName())
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(INSERT_SETTING)
                                    .bind("settingName", this.getInternalName())
                                    .execute();

                            return handle.createQuery(GET_SETTING_ID)
                                    .bind("settingName", this.getInternalName())
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }

    public abstract T parseSetting(String setting);

    public abstract boolean isAllowedValue(String value);
}
