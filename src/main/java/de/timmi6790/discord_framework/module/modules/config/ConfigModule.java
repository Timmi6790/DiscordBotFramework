package de.timmi6790.discord_framework.module.modules.config;

import de.timmi6790.commons.utilities.GsonUtilities;
import de.timmi6790.discord_framework.module.AbstractModule;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Global module config system
 */
@EqualsAndHashCode(callSuper = true)
@Log4j2
public class ConfigModule extends AbstractModule {
    /**
     * Instantiates a new Config module.
     */
    public ConfigModule() {
        super("Config");
    }

    private String getFormattedModuleName(@NonNull final AbstractModule module) {
        return module.getModuleName().replace(' ', '_').toLowerCase();
    }

    private Path getBaseConfigPath() {
        return Paths.get("./configs/");
    }

    private Path getModuleFolderPath(@NonNull final AbstractModule module) {
        return Paths.get(
                this.getBaseConfigPath()
                        + FileSystems.getDefault().getSeparator()
                        + this.getFormattedModuleName(module)
        );
    }

    private Path getModuleConfigPath(@NonNull final AbstractModule module, @NonNull final Class<?> configClass) {
        return Paths.get(
                this.getModuleFolderPath(module)
                        + FileSystems.getDefault().getSeparator()
                        + configClass.getSimpleName().toLowerCase()
                        + ".json"
        );
    }

    /**
     * Creates a new config file /configs/<module>/<config class name>.json
     *
     * @param module the module
     * @param config the config object
     */
    @SneakyThrows
    public void registerConfig(@NonNull final AbstractModule module, @NonNull final Object config) {
        final Path configFolderPath = this.getModuleFolderPath(module);
        Files.createDirectories(configFolderPath);

        final Path configPath = this.getModuleConfigPath(module, config.getClass());
        if (!Files.exists(configPath)) {
            // New file
            GsonUtilities.saveToJson(configPath, config);
            log.info(
                    "Created {} config file {}",
                    module.getModuleName(),
                    config.getClass().getSimpleName()
            );
        } else {
            // TODO: Add a better verify method
            // This will currently always write new configs and remove old ones
            this.saveConfig(module, config.getClass());
        }
    }

    /**
     * Retrieves the config from the configs folder
     *
     * @param <T>         the config type
     * @param module      the module
     * @param configClass the config class
     * @return the config object
     */
    @SneakyThrows
    public <T> T getConfig(@NonNull final AbstractModule module, @NonNull final Class<T> configClass) {
        final T config = GsonUtilities.readJsonFile(this.getModuleConfigPath(module, configClass), configClass);
        log.debug("Loaded {} {} from file.", configClass.getSimpleName(), module.getModuleName());
        return config;
    }

    /**
     * Save the config to file
     *
     * @param module      the module
     * @param configClass the config class
     */
    @SneakyThrows
    public void saveConfig(@NonNull final AbstractModule module, @NonNull final Class<?> configClass) {
        final Object currentConfig = this.getConfig(module, configClass);
        if (currentConfig == null) {
            return;
        }

        GsonUtilities.saveToJson(this.getModuleConfigPath(module, configClass), currentConfig);
    }

    /**
     * Registers the config and also returns the instance
     *
     * @param <T>    the config type parameter
     * @param module the module
     * @param config the config
     * @return the config
     */
    public <T> T registerAndGetConfig(@NonNull final AbstractModule module, @NonNull final T config) {
        this.registerConfig(module, config);
        return (T) this.getConfig(module, config.getClass());
    }
}
