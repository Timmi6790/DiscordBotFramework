package de.timmi6790.discord_framework.modules.new_module_manager.provider.providers.jar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.new_module_manager.provider.ModuleProvider;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Log4j2
public class JarModuleProvider implements ModuleProvider {
    private static final Path MODULE_PATH = Paths.get("./plugins/");

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
    public Collection<Class<? extends Module>> getModules() {
        final File[] pluginJars = MODULE_PATH
                .toFile()
                .listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        final Set<Class<? extends Module>> modules = new HashSet<>();
        if (pluginJars != null) {
            for (final File jar : pluginJars) {
                modules.addAll(this.getModulesFromJar(jar));
            }
        }

        return modules;
    }

    private Optional<Class<?>> getClass(final String path, final URLClassLoader classLoader) {
        try {
            final Class<?> clazz = Class.forName(path, true, classLoader);
            return Optional.of(clazz);
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private List<Class<? extends Module>> getModulesFromJar(final File jar) {
        log.info("Checking {} for modules.", jar.getName());

        final List<Class<? extends Module>> modules = new ArrayList<>();
        try {
            final URLClassLoader child = new URLClassLoader(
                    new URL[]{jar.toURI().toURL()},
                    this.getClass().getClassLoader()
            );

            final URL pluginUrl = child.getResource("plugin.json");
            if (pluginUrl == null) {
                log.warn("Can't load {}, no plugins.json found.", jar.getName());
                return modules;
            }


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

                    if (!Module.class.isAssignableFrom(pluginClassOpt.get())) {
                        log.warn(
                                "Module {} inside {} is not implementing Module!",
                                path,
                                jar.getName()
                        );
                        continue;
                    }

                    final Class<? extends Module> moduleClass = Class.forName(path, true, child)
                            .asSubclass(Module.class);
                    modules.add(moduleClass);
                }
            }
        } catch (final Exception e) {
            log.warn(
                    "Error while trying to load modules from " + jar.getName() + ".",
                    e
            );
        }

        return modules;
    }
}
