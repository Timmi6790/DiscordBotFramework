package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.setting.commands.SettingsCommand;
import de.timmi6790.discord_framework.modules.setting.repository.SettingRepository;
import de.timmi6790.discord_framework.modules.setting.repository.SettingRepositoryMysql;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
public class SettingModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractSetting<?>> settings = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    private SettingRepository settingRepository;

    public SettingModule() {
        super("Setting");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                CommandModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.settingRepository = new SettingRepositoryMysql(this);

        this.getModuleOrThrow(CommandModule.class)
                .registerCommands(
                        this,
                        new SettingsCommand()
                );
    }

    public void registerSetting(final AbstractSetting<?> setting) {
        setting.setDatabaseId(this.settingRepository.retrieveOrCreateSettingId(setting.getInternalName()));

        this.settings.put(setting.getDatabaseId(), setting);
        this.nameIdMatching.put(setting.getName(), setting.getDatabaseId());
    }

    public void registerSettings(final AbstractSetting<?>... settings) {
        for (final AbstractSetting<?> setting : settings) {
            this.registerSetting(setting);
        }
    }

    public Optional<AbstractSetting<?>> getSetting(final String settingName) {
        if (!this.nameIdMatching.containsKey(settingName)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.settings.get(this.nameIdMatching.get(settingName)));
    }

    public <T extends AbstractSetting<?>> Optional<T> getSetting(final Class<T> clazz) {
        for (final AbstractSetting<?> setting : this.settings.values()) {
            if (setting.getClass().equals(clazz)) {
                return (Optional<T>) Optional.of(setting);
            }
        }
        return Optional.empty();
    }

    public Optional<AbstractSetting<?>> getSetting(final int dbId) {
        return Optional.ofNullable(this.settings.get(dbId));
    }
}
