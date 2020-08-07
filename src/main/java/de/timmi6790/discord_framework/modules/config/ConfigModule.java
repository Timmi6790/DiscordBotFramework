package de.timmi6790.discord_framework.modules.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigModule extends AbstractModule {
    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private final Map<Class<?>, Object> configs = new HashMap<>();

    public ConfigModule() {
        super("ConfigModule");
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    private String getFormattedModuleName(final AbstractModule module) {
        return module.getName().replace(" ", "_").toLowerCase();
    }

    private Path getBaseConfigPath() {
        return Paths.get(DiscordBot.getBasePath() + "/configs/");
    }

    private Path getModuleFolderPath(final AbstractModule module) {
        return Paths.get(this.getBaseConfigPath() + FileSystems.getDefault().getSeparator() + this.getFormattedModuleName(module));
    }

    private Path getModuleConfigPath(final AbstractModule module, final Class configClass) {
        return Paths.get(this.getModuleFolderPath(module) + FileSystems.getDefault().getSeparator() + configClass.getSimpleName().toLowerCase() + ".json");
    }

    @SneakyThrows
    public boolean registerConfig(final AbstractModule module, final Object config) {
        final Path configFolderPath = this.getModuleFolderPath(module);
        Files.createDirectories(configFolderPath);

        final Path configPath = this.getModuleConfigPath(module, config.getClass());
        if (!Files.exists(configPath)) {
            // New file
            Files.write(configPath, Collections.singleton(gson.toJson(config)));
            DiscordBot.getLogger().info("Created {} config file {}", module.getName(), config.getClass().getSimpleName());
        } else {
            // TODO: Add a better verify method
            // This will currently always write new configs and remove old ones
            final Object currentConfig = this.getConfig(module, config.getClass());
            Files.write(configPath, Collections.singleton(gson.toJson(currentConfig)));
        }

        return true;
    }

    @SneakyThrows
    public <T> T getConfig(final AbstractModule module, final Class<T> configClass) {
        if (this.configs.containsKey(configClass)) {
            return (T) this.configs.get(configClass);
        }

        final BufferedReader bufferedReader = new BufferedReader(new FileReader(this.getModuleConfigPath(module, configClass).toString()));
        final T config = gson.fromJson(bufferedReader, configClass);
        DiscordBot.getLogger().debug("Loaded {} from file.", configClass.getSimpleName());
        this.configs.put(configClass, config);
        return config;
    }

    public <T> T registerAndGetConfig(final AbstractModule module, final T config) {
        this.registerConfig(module, config);
        return (T) this.getConfig(module, config.getClass());
    }
}
