package de.timmi6790.discord_framework.module.modules.setting;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.setting.repository.SettingRepository;
import de.timmi6790.discord_framework.module.modules.setting.repository.postgres.SettingPostgresRepository;
import de.timmi6790.discord_framework.module.modules.setting.settings.CommandAutoCorrectSetting;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class SettingModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractSetting<?>> settings = new HashMap<>();
    private final Map<String, Integer> nameIdMatching = new HashMap<>();
    private final Map<String, String> aliasNameMatcher = new HashMap<>();

    private SettingRepository settingRepository;
    private PermissionsModule permissionsModule;

    public SettingModule() {
        super("Setting");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                PermissionsModule.class
        );
    }

    protected int getSettingIdOrCreate(final String internalName) {
        return this.settingRepository.getSettingId(internalName)
                .orElseGet(() -> this.settingRepository.createSetting(internalName));
    }

    @Override
    public boolean onInitialize() {
        this.settingRepository = new SettingPostgresRepository(
                this.getModuleOrThrow(DatabaseModule.class).getJdbi()
        );
        this.permissionsModule = this.getModuleOrThrow(PermissionsModule.class);

        this.registerSettings(
                this,
                new CommandAutoCorrectSetting()
        );

        return true;
    }

    public void registerSettings(final AbstractModule module, final AbstractSetting<?>... settings) {
        for (final AbstractSetting<?> setting : settings) {
            this.registerSetting(module, setting);
        }
    }

    public void registerSetting(final AbstractModule module, final AbstractSetting<?> setting) {
        setting.setInternalName(this.generateInternalName(module, "setting", setting.getName()))
                .setDatabaseId(this.getSettingIdOrCreate(setting.getInternalName()))
                .setPermissionId(this.permissionsModule.addPermission(setting.getInternalName()));

        this.settings.put(setting.getDatabaseId(), setting);
        this.nameIdMatching.put(setting.getName(), setting.getDatabaseId());

        for (final String aliasName : setting.getAliasNames()) {
            this.aliasNameMatcher.put(aliasName, setting.getName());
        }
    }

    public Optional<AbstractSetting<?>> getSetting(final String settingName) {
        final String name = this.aliasNameMatcher.getOrDefault(settingName, settingName);
        final Integer settingId = this.nameIdMatching.get(name);
        if (settingId == null) {
            return Optional.empty();
        }

        return this.getSetting(settingId);
    }

    public Optional<AbstractSetting<?>> getSetting(final int settingId) {
        return Optional.ofNullable(this.settings.get(settingId));
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
