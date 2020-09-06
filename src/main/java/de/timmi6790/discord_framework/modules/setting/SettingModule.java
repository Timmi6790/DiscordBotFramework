package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.setting.commands.SettingsCommand;
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
        super("Setting");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                CommandModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.getModuleOrThrow(CommandModule.class)
                .registerCommands(
                        this,
                        new SettingsCommand()
                );
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

    public <D extends AbstractSetting> Optional<AbstractSetting<D>> getSetting(final Class<D> clazz) {
        return Optional.empty();
    }

    public Optional<AbstractSetting<?>> getSetting(final int dbId) {
        return Optional.ofNullable(this.settings.get(dbId));
    }
}
