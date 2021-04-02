package de.timmi6790.discord_framework.module.provider.providers.jar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.provider.ModuleProvider;
import de.timmi6790.discord_framework.utilities.ModuleUtilities;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Log4j2
public class JarModuleProvider implements ModuleProvider {
    private static final Path Module_PATH = Paths.get("./plugins/");

    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    @Override
    public String getName() {
        return "Jar";
    }

    @Override
    public Collection<Class<? extends AbstractModule>> getModules() {
        // Make sure that the path exists
        try {
            Files.createDirectories(Module_PATH);
        } catch (final IOException ignore) {
        }

        final File[] pluginJars = Module_PATH
                .toFile()
                .listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        final Set<Class<? extends AbstractModule>> abstractModules = new HashSet<>();
        if (pluginJars != null) {
            for (final File jar : pluginJars) {
                abstractModules.addAll(this.getAbstractModulesFromJar(jar));
            }
        }

        return abstractModules;
    }

    private Optional<Class<?>> getClass(final String path, final URLClassLoader classLoader) {
        try {
            final Class<?> clazz = Class.forName(path, true, classLoader);
            return Optional.of(clazz);
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private List<Class<? extends AbstractModule>> getAbstractModulesFromJar(final File jar) {
        log.info("Checking {} for Modules.", jar.getName());

        final List<Class<? extends AbstractModule>> abstractModules = new ArrayList<>();
        try (final URLClassLoader child = new URLClassLoader(
                new URL[]{jar.toURI().toURL()},
                this.getClass().getClassLoader()
        )) {
            final URL pluginUrl = child.getResource("plugin.json");
            if (pluginUrl == null) {
                log.warn("Can't load {}, no plugins.json found.", jar.getName());
                return abstractModules;
            }

            ModuleUtilities.addJarToSystemClassLoader(jar);

            try (final BufferedReader inputStream = new BufferedReader(new InputStreamReader(pluginUrl.openStream(), StandardCharsets.UTF_8))) {
                final PluginConfig plugin = gson.fromJson(inputStream, PluginConfig.class);
                for (final String path : plugin.getModules()) {
                    final Optional<Class<?>> pluginClassOpt = this.getClass(path, child);
                    if (!pluginClassOpt.isPresent()) {
                        log.warn(
                                "Can't load Module {} inside {}, unknown path.",
                                path,
                                jar.getName()
                        );
                        continue;
                    }

                    if (!AbstractModule.class.isAssignableFrom(pluginClassOpt.get())) {
                        log.warn(
                                "Module {} inside {} is not implementing Module!",
                                path,
                                jar.getName()
                        );
                        continue;
                    }

                    final Class<? extends AbstractModule> moduleClass = Class.forName(path, true, child)
                            .asSubclass(AbstractModule.class);
                    abstractModules.add(moduleClass);
                }
            }
        } catch (final Exception e) {
            log.warn(
                    "Error while trying to load Modules from " + jar.getName() + ".",
                    e
            );
        }

        return abstractModules;
    }
}
