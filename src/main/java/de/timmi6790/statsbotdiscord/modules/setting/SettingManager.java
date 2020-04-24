package de.timmi6790.statsbotdiscord.modules.setting;

import de.timmi6790.statsbotdiscord.StatsBot;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SettingManager {
    private final Map<Integer, AbstractSetting> settings = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public void registerSetting(final AbstractSetting setting) {
        setting.setDbId(this.getSettingId(setting.getInternalName())
                .orElseGet(() -> StatsBot.getDatabase().withHandle(handle -> {
                    handle.createUpdate("INSERT INTO setting(setting_name) VALUES(:internalName);")
                            .bind("internalName", setting.getInternalName())
                            .execute();

                    return handle.createUpdate("SELECT LAST_INSERT_ID();").execute();
                }))
        );

        this.settings.put(setting.getDbId(), setting);
        this.nameIdMatching.put(setting.getInternalName(), setting.getDbId());
    }

    public void registerSettings(final AbstractSetting... settings) {
        for (final AbstractSetting setting : settings) {
            this.registerSetting(setting);
        }
    }

    public Optional<AbstractSetting> getSetting(final String internalName) {
        if (!this.nameIdMatching.containsKey(internalName)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.settings.get(this.nameIdMatching.get(internalName)));
    }

    public Optional<AbstractSetting> getSetting(final int dbId) {
        return Optional.ofNullable(this.settings.get(dbId));
    }

    private Optional<Integer> getSettingId(final String internalName) {
        return StatsBot.getDatabase().withHandle(handle -> handle.createQuery("SELECT id FROM setting WHERE setting_name = :internalName LIMIT 1;")
                .bind("internalName", internalName)
                .mapTo(Integer.class)
                .findFirst());
    }
}
