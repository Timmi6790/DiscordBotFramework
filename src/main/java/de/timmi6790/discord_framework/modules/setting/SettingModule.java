package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.setting.commands.SettingsCommand;
import de.timmi6790.discord_framework.modules.setting.repository.SettingRepository;
import de.timmi6790.discord_framework.modules.setting.repository.SettingRepositoryMysql;
import de.timmi6790.discord_framework.modules.setting.settings.CommandAutoCorrectSetting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
public class SettingModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractSetting<?>> settings = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = Collections.synchronizedMap(new CaseInsensitiveMap<>());

    private SettingRepository settingRepository;

    public SettingModule() {
        super("Setting");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                CommandModule.class,
                PermissionsModule.class
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

        this.registerSettings(
                this,
                new CommandAutoCorrectSetting()
        );
    }

    public void registerSetting(final AbstractModule module, final AbstractSetting<?> setting) {
        setting.setDatabaseId(this.settingRepository.retrieveOrCreateSettingId(setting.getInternalName()));
        final String defaultPermissionName = String.format("%s.setting.%s", module.getName(), setting.getName())
                .replace(" ", "_")
                .toLowerCase();
        setting.setPermissionId(AbstractCommand.getPermissionsModule().addPermission(defaultPermissionName));

        this.settings.put(setting.getDatabaseId(), setting);
        this.nameIdMatching.put(setting.getName(), setting.getDatabaseId());
    }

    public void registerSettings(final AbstractModule module, final AbstractSetting<?>... settings) {
        for (final AbstractSetting<?> setting : settings) {
            this.registerSetting(module, setting);
        }
    }

    public Optional<AbstractSetting<?>> getSetting(final String settingName) {
        if (!this.nameIdMatching.containsKey(settingName)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.settings.get(this.nameIdMatching.get(settingName)));
    }

    public <T> Optional<? extends AbstractSetting<T>> getSetting(final Class<? extends AbstractSetting<T>> clazz) {
        for (final AbstractSetting<?> setting : this.settings.values()) {
            if (setting.getClass().equals(clazz)) {
                return Optional.of((AbstractSetting<T>) setting);
            }
        }
        return Optional.empty();
    }

    public Optional<AbstractSetting<?>> getSetting(final int dbId) {
        return Optional.ofNullable(this.settings.get(dbId));
    }
}
