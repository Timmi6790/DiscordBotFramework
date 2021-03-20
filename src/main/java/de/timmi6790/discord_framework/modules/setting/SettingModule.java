package de.timmi6790.discord_framework.modules.setting;


import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.setting.repository.SettingRepository;
import de.timmi6790.discord_framework.modules.setting.repository.mysql.SettingRepositoryMysql;
import de.timmi6790.discord_framework.modules.setting.settings.CommandAutoCorrectSetting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode
@Log4j2
public class SettingModule implements Module {
    @Getter
    private final Map<Integer, AbstractSetting<?>> settings = new HashMap<>();
    private final Map<String, Integer> nameIdMatching = new CaseInsensitiveMap<>();
    private final Map<String, String> aliasNameMatcher = new CaseInsensitiveMap<>();

    private final SettingRepository settingRepository;
    private final PermissionsModule permissionsModule;

    public SettingModule(final SettingRepositoryMysql settingRepository, final PermissionsModule permissionsModule) {
        System.out.println(settingRepository + " " + permissionsModule);

        this.settingRepository = settingRepository;
        this.permissionsModule = permissionsModule;

        this.registerSettings(
                this,
                new CommandAutoCorrectSetting()
        );
    }

    @Override
    public String getName() {
        return "Setting";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
    }

    public void registerSettings(final Module module, final AbstractSetting<?>... settings) {
        for (final AbstractSetting<?> setting : settings) {
            this.registerSetting(module, setting);
        }
    }

    public void registerSetting(final Module module, final AbstractSetting<?> setting) {
        log.debug("Register setting {}", setting.getStatName());
        setting.setInternalName(this.generateInternalName(module, "setting", setting.getStatName()))
                .setDatabaseId(this.settingRepository.retrieveOrCreateSettingId(setting.getInternalName()))
                .setPermissionId(this.permissionsModule.addPermission(setting.getInternalName()));

        synchronized (this.settings) {
            this.settings.put(setting.getDatabaseId(), setting);
        }
        synchronized (this.nameIdMatching) {
            this.nameIdMatching.put(setting.getStatName(), setting.getDatabaseId());
        }

        synchronized (this.aliasNameMatcher) {
            for (final String aliasName : setting.getAliasNames()) {
                this.aliasNameMatcher.put(aliasName, setting.getStatName());
            }
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
