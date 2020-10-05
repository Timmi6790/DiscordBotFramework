package de.timmi6790.discord_framework.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.commons.utilities.ReflectionUtilities;
import de.timmi6790.discord_framework.datatypes.sorting.TopicalSort;
import de.timmi6790.discord_framework.exceptions.ModuleNotFoundException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.sentry.Sentry;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import org.tinylog.TaggedLogger;

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
public class ModuleManager {
    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private final Map<Class<? extends AbstractModule>, AbstractModule> loadedModules = new HashMap<>();
    private final Set<Class<? extends AbstractModule>> initializedModules = new HashSet<>();
    private final Set<Class<? extends AbstractModule>> startedModules = new HashSet<>();

    private final TaggedLogger logger;
    private final Path pluginPath;

    @SneakyThrows
    public ModuleManager(final TaggedLogger logger) {
        this.logger = logger;

        this.pluginPath = Paths.get(Paths.get(".").toAbsolutePath().normalize() + "/plugins/");
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
                final Optional<Class<? extends AbstractModule>> missingDependency = module.getDependencies()
                        .parallelStream()
                        .filter(dependencyClass -> !moduleClasses.contains(dependencyClass))
                        .findAny();

                if (missingDependency.isPresent()) {
                    this.logger.warn("Can't load {}, because it is missing the {} dependency.", module.getName(), missingDependency.get().getSimpleName());

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
            module.getLoadAfter()
                    .stream()
                    .map(moduleClasses::indexOf)
                    .filter(dependencyIndex -> dependencyIndex != -1)
                    .map(dependencyIndex -> new TopicalSort.Dependency(moduleIndex, dependencyIndex))
                    .forEach(edges::add);

            // Load before
            module.getLoadBefore()
                    .stream()
                    .map(moduleClasses::indexOf)
                    .filter(dependencyIndex -> dependencyIndex != -1)
                    .map(dependencyIndex -> new TopicalSort.Dependency(dependencyIndex, moduleIndex))
                    .forEach(edges::add);
        }

        final TopicalSort<Class<? extends AbstractModule>> moduleSort = new TopicalSort<>(moduleClasses, edges);
        return moduleSort.sort();
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private Set<AbstractModule> getExternalModules() {
        final Set<AbstractModule> abstractModules = new HashSet<>();

        final File[] pluginJars = this.pluginPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (pluginJars == null) {
            return abstractModules;
        }

        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        for (final File jar : pluginJars) {
            this.logger.info("Checking {} for modules.", jar.getName());

            try {
                // Add external jar to system classloader
                ReflectionUtilities.addJarToClassLoader(jar, classLoader);

                final URL pluginUrl = classLoader.getResource("plugin.json");
                if (pluginUrl == null) {
                    this.logger.warn("Can't load {}, no plugins.json found.", jar.getName());
                    continue;
                }

                @Cleanup final InputStreamReader inputStream = new InputStreamReader(pluginUrl.openStream(), StandardCharsets.UTF_8);
                final PluginConfig plugin = gson.fromJson(inputStream, PluginConfig.class);
                for (final String path : plugin.getModules()) {
                    final Optional<Class<?>> pluginClassOpt = ReflectionUtilities.loadClassFromClassLoader(path, classLoader);
                    if (!pluginClassOpt.isPresent()) {
                        this.logger.warn("Can't load Module {} inside {}, unknown path.", path, jar.getName());
                        continue;
                    }

                    if (!AbstractModule.class.isAssignableFrom(pluginClassOpt.get())) {
                        this.logger.warn("Module {} inside {} is not extending AbstractModule!", path, jar.getName());
                        continue;
                    }

                    final AbstractModule pluginModule = Class.forName(path, true, classLoader)
                            .asSubclass(AbstractModule.class)
                            .getConstructor()
                            .newInstance();

                    if (abstractModules.contains(pluginModule)) {
                        this.logger.warn("Module {} inside {} is already loaded.", pluginModule.getName(), jar.getName());
                        continue;
                    }

                    this.logger.info("Found module {} inside {}", pluginModule.getName(), jar.getName());
                    abstractModules.add(pluginModule);
                }

            } catch (final Exception e) {
                this.logger.warn(e, "Error while trying to load modules from {}.", jar.getName());
            }
        }

        return abstractModules;
    }

    public void registerModules(final AbstractModule... modules) {
        for (final AbstractModule module : modules) {
            this.registerModule(module);
        }
    }

    public boolean registerModule(final AbstractModule module) {
        if (this.loadedModules.containsKey(module.getClass())) {
            this.logger.debug("{} is already registered!", module.getName());
            return false;
        }

        this.logger.debug("Registered {}[{}]", module.getName(), module.getClass());
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
            this.logger.warn("Tried to initialize {} while already being started.", moduleClass);
            return false;
        }

        if (this.initializedModules.contains(moduleClass)) {
            this.logger.warn("Tried to initialize {} while already being initialized.", moduleClass);
            return false;
        }

        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null) {
            this.logger.warn("Tried to start {} while not being loaded.", moduleClass);
            return false;
        }

        // Check if all load dependencies are innitlized
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfter()) {
            if (!this.initializedModules.contains(dependencyClass)) {
                this.logger.warn("Tried to initialize {} without {} dependency being initialized.", moduleClass, dependencyClass);
                return false;
            }
        }

        this.logger.info("Initialize module {}", module.getName());
        try {
            module.onInitialize();
            this.initializedModules.add(moduleClass);
            return true;
        } catch (final Exception e) {
            this.logger.error(module.getName(), e);
            Sentry.captureException(e);

            return false;
        }
    }

    public boolean start(final Class<? extends AbstractModule> moduleClass) {
        if (this.startedModules.contains(moduleClass)) {
            this.logger.warn("Tried to start {} while already being started.", moduleClass);
            return false;
        }

        if (!this.initializedModules.contains(moduleClass)) {
            this.logger.warn("Tried to start {} while not being initialized.", moduleClass);
            return false;
        }

        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null) {
            this.logger.warn("Tried to start {} while not being loaded.", moduleClass);
            return false;
        }

        // Check if all load dependencies are started
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfter()) {
            if (!(this.initializedModules.contains(dependencyClass) || this.startedModules.contains(dependencyClass))) {
                this.logger.warn("Tried to start {} without {} dependency being started", moduleClass, dependencyClass);
                return false;
            }
        }

        this.logger.info("Starting module {}", module.getName());
        try {
            module.onEnable();
            this.startedModules.add(moduleClass);
            this.initializedModules.remove(moduleClass);
            return true;
        } catch (final Exception e) {
            this.logger.error(module.getName(), e);
            Sentry.captureException(e);

            return false;
        }
    }

    public void initializeAll() throws TopicalSortCycleException {
        this.getSortedModules().forEach(this::initialize);
    }

    public void startAll() throws TopicalSortCycleException {
        this.getSortedModules().forEach(this::start);
    }

    public void loadExternalModules() {
        this.getExternalModules().forEach(this::registerModule);
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
            this.logger.error(module.getName(), e);
            Sentry.captureException(e);

            return false;
        }
    }
}
