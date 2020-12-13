package de.timmi6790.discord_framework.modules.config;

import de.timmi6790.commons.utilities.GsonUtilities;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class ConfigModule extends AbstractModule {
    private final Map<Class<?>, Object> configs = new HashMap<>();

    public ConfigModule() {
        super("Config");
    }

    private String getFormattedModuleName(@NonNull final AbstractModule module) {
        return module.getName().replace(' ', '_').toLowerCase();
    }

    private Path getBaseConfigPath() {
        return Paths.get(this.getDiscordBot().getBasePath() + "/configs/");
    }

    private Path getModuleFolderPath(@NonNull final AbstractModule module) {
        return Paths.get(
                this.getBaseConfigPath()
                        + FileSystems.getDefault().getSeparator()
                        + this.getFormattedModuleName(module)
        );
    }

    private Path getModuleConfigPath(@NonNull final AbstractModule module, @NonNull final Class configClass) {
        return Paths.get(
                this.getModuleFolderPath(module)
                        + FileSystems.getDefault().getSeparator()
                        + configClass.getSimpleName().toLowerCase()
                        + ".json"
        );
    }

    @SneakyThrows
    public boolean registerConfig(@NonNull final AbstractModule module, @NonNull final Object config) {
        final Path configFolderPath = this.getModuleFolderPath(module);
        Files.createDirectories(configFolderPath);

        final Path configPath = this.getModuleConfigPath(module, config.getClass());
        if (!Files.exists(configPath)) {
            // New file
            GsonUtilities.saveToJson(configPath, config);
            DiscordBot.getLogger().info("Created {} config file {}", module.getName(), config.getClass().getSimpleName());
        } else {
            // TODO: Add a better verify method
            // This will currently always write new configs and remove old ones
            this.saveConfig(module, config.getClass());
        }

        return true;
    }

    @SneakyThrows
    public <T> T getConfig(@NonNull final AbstractModule module, @NonNull final Class<T> configClass) {
        T savedConfig = (T) configs.get(configClass);
        if (savedConfig != null) {
            return savedConfig;
        }
        
        final T config = GsonUtilities.readJsonFile(this.getModuleConfigPath(module, configClass), configClass);
        DiscordBot.getLogger().debug("Loaded {} {} from file.", configClass.getSimpleName(), module.getName());
        this.configs.put(configClass, config);
        return config;
    }

    @SneakyThrows
    public void saveConfig(@NonNull final AbstractModule module, @NonNull final Class<?> configClass) {
        final Object currentConfig = this.getConfig(module, configClass);
        if (currentConfig == null) {
            return;
        }

        GsonUtilities.saveToJson(this.getModuleConfigPath(module, configClass), currentConfig);
    }

    public <T> T registerAndGetConfig(@NonNull final AbstractModule module, @NonNull final T config) {
        this.registerConfig(module, config);
        return (T) this.getConfig(module, config.getClass());
    }
}
