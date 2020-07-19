package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.AbstractModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
public class SettingModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractSetting<?>> settings = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public SettingModule() {
        super("SettingModule");
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public void registerSetting(final AbstractSetting<?> setting) {
        this.settings.put(setting.getDatabaseId(), setting);
        this.nameIdMatching.put(setting.getInternalName(), setting.getDatabaseId());
    }

    public void registerSettings(final AbstractSetting<?>... settings) {
        Arrays.stream(settings).forEach(this::registerSetting);
    }

    public Optional<AbstractSetting<?>> getSetting(final String internalName) {
        if (!this.nameIdMatching.containsKey(internalName)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.settings.get(this.nameIdMatching.get(internalName)));
    }

    public Optional<AbstractSetting<?>> getSetting(final int dbId) {
        return Optional.ofNullable(this.settings.get(dbId));
    }
}
