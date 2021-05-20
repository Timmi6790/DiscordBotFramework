package de.timmi6790.discord_framework.module.provider.providers.jar;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.provider.ModuleProvider;
import io.sentry.Sentry;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.net.URL;
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

    private String getLogName(final ModuleReference moduleReference) {
        return moduleReference.location()
                .map(URI::getRawQuery)
                .orElseGet(() -> moduleReference.descriptor().name());
    }

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

        try {
            final ModuleFinder finder = ModuleFinder.of(Module_PATH);

            final Set<ModuleReference> moduleReferences = finder.findAll();
            final Set<String> moduleNames = Sets.newHashSetWithExpectedSize(moduleReferences.size());
            for (final ModuleReference moduleReference : moduleReferences) {
                moduleNames.add(moduleReference.descriptor().name());
            }

            final ModuleLayer parentLayer = ModuleLayer.boot();
            final Configuration configuration = parentLayer.configuration().resolve(finder, ModuleFinder.of(), moduleNames);
            final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            final ModuleLayer layer = parentLayer.defineModulesWithManyLoaders(configuration, systemClassLoader);

            final Set<Class<? extends AbstractModule>> abstractModules = new HashSet<>();
            for (final ModuleReference moduleReference : moduleReferences) {
                abstractModules.addAll(this.getAbstractModulesFromJar(moduleReference, layer));
            }

            return abstractModules;
        } catch (final Exception e) {
            log.catching(e);
            Sentry.captureException(e);
            return new HashSet<>();
        }
    }

    private Optional<Class<?>> getClass(final ModuleReference moduleReference,
                                        final ModuleLayer moduleLayer,
                                        final String path) {
        try {
            final Class<?> clazz = moduleLayer.findLoader(moduleReference.descriptor().name())
                    .loadClass(path);
            return Optional.of(clazz);
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<PluginConfig> getPluginConfig(final URL pluginUrl) {
        if (pluginUrl == null) {
            return Optional.empty();
        }

        try (final BufferedReader inputStream = new BufferedReader(new InputStreamReader(pluginUrl.openStream(), StandardCharsets.UTF_8))) {
            return Optional.ofNullable(gson.fromJson(inputStream, PluginConfig.class));
        } catch (final IOException ignore) {
            return Optional.empty();
        }
    }

    private List<Class<? extends AbstractModule>> getAbstractModulesFromJar(final ModuleReference moduleReference,
                                                                            final ModuleLayer moduleLayer) {
        log.info(
                "Checking {} for Modules.",
                this.getLogName(moduleReference)
        );

        final List<Class<? extends AbstractModule>> modules = new ArrayList<>();
        try {
            final URL pluginUrl = moduleLayer.findLoader(moduleReference.descriptor().name()).getResource("plugin.json");
            final Optional<PluginConfig> pluginConfigOpt = this.getPluginConfig(pluginUrl);
            if (pluginConfigOpt.isEmpty()) {
                log.warn(
                        "Can't load {}, no plugins.json found.",
                        this.getLogName(moduleReference)
                );
                return modules;
            }

            final PluginConfig plugin = pluginConfigOpt.get();
            for (final String path : plugin.getModules()) {
                final Optional<Class<?>> pluginClassOpt = this.getClass(moduleReference, moduleLayer, path);
                if (pluginClassOpt.isEmpty()) {
                    log.warn(
                            "Can't load Module {} inside {}, unknown path.",
                            path,
                            this.getLogName(moduleReference)
                    );
                    continue;
                }

                if (!AbstractModule.class.isAssignableFrom(pluginClassOpt.get())) {
                    log.warn(
                            "Module {} inside {} is not implementing Module!",
                            path,
                            this.getLogName(moduleReference)
                    );
                    continue;
                }

                final Class<? extends AbstractModule> moduleClass = pluginClassOpt.get()
                        .asSubclass(AbstractModule.class);
                modules.add(moduleClass);
            }
        } catch (final Exception e) {
            log.warn(
                    "Error while trying to load Modules from " + this.getLogName(moduleReference) + ".",
                    e
            );
        }

        return modules;
    }
}
