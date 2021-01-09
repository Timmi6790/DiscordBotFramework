package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.setting.repository.SettingRepository;
import de.timmi6790.discord_framework.modules.setting.repository.mysql.SettingRepositoryMysql;
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
    private final Map<String, String> aliasNameMatcher = Collections.synchronizedMap(new CaseInsensitiveMap<>());

    private SettingRepository settingRepository;
    private PermissionsModule permissionsModule;

    public SettingModule() {
        super("Setting");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                PermissionsModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.settingRepository = new SettingRepositoryMysql(this);
        this.permissionsModule = this.getModuleOrThrow(PermissionsModule.class);

        this.registerSettings(
                this,
                new CommandAutoCorrectSetting()
        );
    }

    public void registerSetting(final AbstractModule module, final AbstractSetting<?> setting) {
        setting.setInternalName(this.generateInternalName(module, "setting", setting.getStatName()));
        setting.setDatabaseId(this.settingRepository.retrieveOrCreateSettingId(setting.getInternalName()));
        setting.setPermissionId(this.permissionsModule.addPermission(setting.getInternalName()));

        this.settings.put(setting.getDatabaseId(), setting);
        this.nameIdMatching.put(setting.getStatName(), setting.getDatabaseId());

        for (final String aliasName : setting.getAliasNames()) {
            this.aliasNameMatcher.put(aliasName, setting.getStatName());
        }
    }

    public void registerSettings(final AbstractModule module, final AbstractSetting<?>... settings) {
        for (final AbstractSetting<?> setting : settings) {
            this.registerSetting(module, setting);
        }
    }

    public Optional<AbstractSetting<?>> getSetting(final String settingName) {
        final String name = this.aliasNameMatcher.getOrDefault(settingName, settingName);
        if (!this.nameIdMatching.containsKey(name)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.settings.get(this.nameIdMatching.get(name)));
    }

    public <T> Optional<? extends AbstractSetting<T>> getSetting(final Class<? extends AbstractSetting<T>> clazz) {
        for (final AbstractSetting<?> setting : this.settings.values()) {
            if (setting.getClass().equals(clazz)) {
                return Optional.of((AbstractSetting<T>) setting);
            }
        }
        return Optional.empty();
    }
}
