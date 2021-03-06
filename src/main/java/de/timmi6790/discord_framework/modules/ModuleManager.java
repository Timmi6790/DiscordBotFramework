package de.timmi6790.discord_framework.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.commons.utilities.ReflectionUtilities;
import de.timmi6790.discord_framework.exceptions.ModuleNotFoundException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.utilities.TopicalSort;
import io.sentry.Sentry;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Global module manager. All models are registered,initialized and started over this manager.
 */
@Data
@Log4j2
public class ModuleManager {
    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private final Map<Class<? extends AbstractModule>, AbstractModule> loadedModules = new HashMap<>();
    private final Set<Class<? extends AbstractModule>> initializedModules = new HashSet<>();
    private final Set<Class<? extends AbstractModule>> startedModules = new HashSet<>();

    private final Path pluginPath;

    @SneakyThrows
    public ModuleManager() {
        this.pluginPath = Paths.get("./plugins/");
        Files.createDirectories(this.pluginPath);
    }

    private List<Class<? extends AbstractModule>> getSortedModules() throws TopicalSortCycleException {
        final List<AbstractModule> modules = new ArrayList<>(this.loadedModules.values());
        final List<Class<? extends AbstractModule>> moduleClasses = new ArrayList<>(this.loadedModules.keySet());

        // Dependency check
        Iterator<AbstractModule> moduleIterator;
        boolean foundMissing;
        do {
            foundMissing = false;
            moduleIterator = modules.iterator();
            while (moduleIterator.hasNext()) {
                final AbstractModule module = moduleIterator.next();
                final List<String> missingDependencies = new ArrayList<>();
                for (final Class<? extends AbstractModule> dependencyModule : module.getDependencies()) {
                    if (!moduleClasses.contains(dependencyModule)) {
                        missingDependencies.add(dependencyModule.getSimpleName());
                    }
                }

                if (!missingDependencies.isEmpty()) {
                    log.warn(
                            "Can't load {}, because it is missing the {} dependencies.",
                            module.getModuleName(),
                            String.join(",", missingDependencies)
                    );

                    moduleClasses.remove(module.getClass());
                    moduleIterator.remove();
                    foundMissing = true;
                }
            }
        } while (foundMissing);

        // Sort modules after load after order
        final List<TopicalSort.Dependency> edges = new ArrayList<>();
        int index = 0;
        for (final AbstractModule module : modules) {
            final int moduleIndex = index++;

            // Load after
            for (final Class<? extends AbstractModule> loadAfterClass : module.getLoadAfterDependencies()) {
                final int loadIndex = moduleClasses.indexOf(loadAfterClass);
                if (loadIndex != -1) {
                    edges.add(new TopicalSort.Dependency(moduleIndex, loadIndex));
                }
            }

            // Load before
            for (final Class<? extends AbstractModule> loadBeforeClass : module.getLoadBeforeDependencies()) {
                final int loadIndex = moduleClasses.indexOf(loadBeforeClass);
                if (loadIndex != -1) {
                    edges.add(new TopicalSort.Dependency(loadIndex, moduleIndex));
                }
            }
        }

        final TopicalSort<Class<? extends AbstractModule>> moduleSort = new TopicalSort<>(moduleClasses, edges);
        return moduleSort.sort();
    }

    private Set<AbstractModule> getExternalModules() {
        final Set<AbstractModule> abstractModules = new HashSet<>();

        final File[] pluginJars = this.pluginPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (pluginJars == null) {
            return abstractModules;
        }

        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        for (final File jar : pluginJars) {
            for (final AbstractModule foundModule : this.loadJarModule(jar, classLoader)) {
                if (abstractModules.contains(foundModule)) {
                    log.warn(
                            "Module {} inside {} is already loaded.",
                            foundModule.getModuleName(),
                            jar.getName()
                    );
                    continue;
                }

                log.info(
                        "Found module {} inside {}",
                        foundModule.getModuleName(),
                        jar.getName()
                );
                abstractModules.add(foundModule);
            }
        }

        return abstractModules;
    }

    private List<AbstractModule> loadJarModule(final File jar, final URLClassLoader classLoader) {
        log.info("Checking {} for modules.", jar.getName());

        final List<AbstractModule> modules = new ArrayList<>();
        try {
            // Add external jar to system classloader
            ReflectionUtilities.addJarToClassLoader(jar, classLoader);

            final URL pluginUrl = classLoader.getResource("plugin.json");
            if (pluginUrl == null) {
                log.warn("Can't load {}, no plugins.json found.", jar.getName());
                return modules;
            }

            @Cleanup final InputStreamReader inputStream = new InputStreamReader(pluginUrl.openStream(), StandardCharsets.UTF_8);
            final PluginConfig plugin = gson.fromJson(inputStream, PluginConfig.class);
            for (final String path : plugin.getModules()) {
                final Optional<Class<?>> pluginClassOpt = ReflectionUtilities.loadClassFromClassLoader(path, classLoader);
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
                            "Module {} inside {} is not extending AbstractModule!",
                            path,
                            jar.getName()
                    );
                    continue;
                }

                final AbstractModule pluginModule = Class.forName(path, true, classLoader)
                        .asSubclass(AbstractModule.class)
                        .getConstructor()
                        .newInstance();

                modules.add(pluginModule);
            }

        } catch (final Exception e) {
            log.warn(
                    "Error while trying to load modules from " + jar.getName() + ".",
                    e
            );
        }

        return modules;
    }

    public void registerModules(final AbstractModule... modules) {
        for (final AbstractModule module : modules) {
            this.registerModule(module);
        }
    }

    public boolean registerModule(final AbstractModule module) {
        if (this.loadedModules.containsKey(module.getClass())) {
            log.debug("{} is already registered!", module.getModuleName());
            return false;
        }

        log.debug("Registered {}[{}]", module.getModuleName(), module.getClass());
        this.loadedModules.put(module.getClass(), module);
        return true;
    }

    public <T extends AbstractModule> Optional<T> getModule(final Class<T> clazz) {
        return (Optional<T>) Optional.ofNullable(this.loadedModules.get(clazz));
    }

    public <T extends AbstractModule> T getModuleOrThrow(final Class<T> clazz) {
        return this.getModule(clazz).orElseThrow(() -> new ModuleNotFoundException(String.valueOf(clazz)));
    }

    public boolean initialize(final Class<? extends AbstractModule> moduleClass) {
        if (this.startedModules.contains(moduleClass)) {
            log.warn("Tried to initialize {} while already being started.", moduleClass);
            return false;
        }

        if (this.initializedModules.contains(moduleClass)) {
            log.warn("Tried to initialize {} while already being initialized.", moduleClass);
            return false;
        }

        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null) {
            log.warn("Tried to start {} while not being loaded.", moduleClass);
            return false;
        }

        // Check if all load dependencies are innitlized
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfterDependencies()) {
            if (!this.initializedModules.contains(dependencyClass)) {
                log.warn(
                        "Tried to initialize {} without {} dependency being initialized.",
                        moduleClass,
                        dependencyClass
                );
                return false;
            }
        }

        log.info("Initialize module {}", module.getModuleName());
        try {
            final boolean initializeStatus = module.onInitialize();
            if (initializeStatus) {
                this.initializedModules.add(moduleClass);
                return true;
            } else {
                log.warn(
                        "{} returned false while trying to initialize it.",
                        module.getModuleName()
                );
                return false;
            }
        } catch (final Exception e) {
            log.error(module.getModuleName(), e);
            Sentry.captureException(e);

            return false;
        }
    }

    public boolean start(final Class<? extends AbstractModule> moduleClass) {
        if (this.startedModules.contains(moduleClass)) {
            log.warn("Tried to start {} while already being started.", moduleClass);
            return false;
        }

        if (!this.initializedModules.contains(moduleClass)) {
            log.warn("Tried to start {} while not being initialized.", moduleClass);
            return false;
        }

        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null) {
            log.warn("Tried to start {} while not being loaded.", moduleClass);
            return false;
        }

        // Check if all load dependencies are started
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfterDependencies()) {
            if (!(this.initializedModules.contains(dependencyClass) || this.startedModules.contains(dependencyClass))) {
                log.warn(
                        "Tried to start {} without {} dependency being started",
                        moduleClass,
                        dependencyClass
                );
                return false;
            }
        }

        log.info("Starting module {}", module.getModuleName());
        try {
            final boolean enableStatus = module.onEnable();
            if (enableStatus) {
                this.startedModules.add(moduleClass);
                this.initializedModules.remove(moduleClass);
                return true;
            } else {
                log.warn(
                        "{} returned false while trying to enable it.",
                        module.getModuleName()
                );
                return false;
            }
        } catch (final Exception e) {
            log.error(module.getModuleName(), e);
            Sentry.captureException(e);
            return false;
        }
    }

    public void initializeAll() throws TopicalSortCycleException {
        for (final Class<? extends AbstractModule> moduleClass : this.getSortedModules()) {
            this.initialize(moduleClass);
        }
    }

    @SneakyThrows
    public void startAll() {
        for (final Class<? extends AbstractModule> moduleClass : this.getSortedModules()) {
            this.start(moduleClass);
        }
    }

    public void loadExternalModules() {
        for (final AbstractModule module : this.getExternalModules()) {
            this.registerModule(module);
        }
    }

    public boolean stopModule(final Class<? extends AbstractModule> moduleClass) {
        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null || !this.startedModules.contains(moduleClass)) {
            return false;
        }

        try {
            module.onDisable();
            this.startedModules.remove(moduleClass);
            return true;
        } catch (final Exception e) {
            log.error(module.getModuleName(), e);
            Sentry.captureException(e);

            return false;
        }
    }
}
