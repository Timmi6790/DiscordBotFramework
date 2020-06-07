package de.timmi6790.statsbotdiscord.modules.setting;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SettingManager {
    @Getter
    private final Map<Integer, AbstractSetting> settings = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public void registerSetting(final AbstractSetting setting) {
        this.settings.put(setting.getDatabaseId(), setting);
        this.nameIdMatching.put(setting.getInternalName(), setting.getDatabaseId());
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
}
