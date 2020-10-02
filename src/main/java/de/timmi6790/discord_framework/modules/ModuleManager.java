package de.timmi6790.discord_framework.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.commons.utilities.ReflectionUtilities;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.datatypes.sorting.TopicalSort;
import de.timmi6790.discord_framework.exceptions.ModuleGetException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.sentry.Sentry;
import lombok.Cleanup;
import lombok.Data;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public boolean registerModule(final AbstractModule module) {
        if (this.loadedModules.containsKey(module.getClass())) {
            DiscordBot.getLogger().debug("{} is already registered!", module.getName());
            return false;
        }

        DiscordBot.getLogger().debug("Registered {}[{}]", module.getName(), module.getClass());
        this.loadedModules.put(module.getClass(), module);
        return true;
    }

    public void registerModules(final AbstractModule... modules) {
        Arrays.stream(modules).forEach(this::registerModule);
    }

    public <T extends AbstractModule> Optional<T> getModule(final Class<T> clazz) {
        return (Optional<T>) Optional.ofNullable(this.loadedModules.get(clazz));
    }

    public <T extends AbstractModule> T getModuleOrThrow(final Class<T> clazz) {
        return this.getModule(clazz).orElseThrow(() -> new ModuleGetException(String.valueOf(clazz)));
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
                    DiscordBot.getLogger().warn("Can't load {}, because it is missing the {} dependency.", module.getName(), missingDependency.get().getSimpleName());

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

        final File[] pluginJars = new File(DiscordBot.getInstance().getBasePath().toString() + "/plugins/").listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (pluginJars == null) {
            return abstractModules;
        }

        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        for (final File jar : pluginJars) {
            DiscordBot.getLogger().info("Checking {} for modules.", jar.getName());

            try {
                // Add external jar to system classloader
                ReflectionUtilities.addJarToClassLoader(jar, classLoader);

                final URL pluginUrl = classLoader.getResource("plugin.json");
                if (pluginUrl == null) {
                    DiscordBot.getLogger().warn("Can't load {}, no plugins.json found.", jar.getName());
                    continue;
                }

                @Cleanup final InputStreamReader inputStream = new InputStreamReader(pluginUrl.openStream(), StandardCharsets.UTF_8);
                final PluginConfig plugin = gson.fromJson(inputStream, PluginConfig.class);
                for (final String path : plugin.getModules()) {
                    final Optional<Class<?>> pluginClassOpt = ReflectionUtilities.loadClassFromClassLoader(path, classLoader);
                    if (!pluginClassOpt.isPresent()) {
                        DiscordBot.getLogger().warn("Can't load Module {} inside {}, unknown path.", path, jar.getName());
                        continue;
                    }

                    if (!AbstractModule.class.isAssignableFrom(pluginClassOpt.get())) {
                        DiscordBot.getLogger().warn("Module {} inside {} is not extending AbstractModule!", path, jar.getName());
                        continue;
                    }

                    final AbstractModule pluginModule = Class.forName(path, true, classLoader)
                            .asSubclass(AbstractModule.class)
                            .getConstructor()
                            .newInstance();

                    if (abstractModules.contains(pluginModule)) {
                        DiscordBot.getLogger().warn("Module {} inside {} is already loaded.", pluginModule.getName(), jar.getName());
                        continue;
                    }

                    DiscordBot.getLogger().info("Found module {} inside {}", pluginModule.getName(), jar.getName());
                    abstractModules.add(pluginModule);
                }

            } catch (final Exception e) {
                DiscordBot.getLogger().warn(e, "Error while trying to load modules from {}.", jar.getName());
            }
        }

        return abstractModules;
    }

    public boolean initialize(final Class<? extends AbstractModule> moduleClass) {
        if (this.startedModules.contains(moduleClass)) {
            DiscordBot.getLogger().warn("Tried to initialize {} while already being started.", moduleClass);
            return false;
        }

        if (this.initializedModules.contains(moduleClass)) {
            DiscordBot.getLogger().warn("Tried to initialize {} while already being initialized.", moduleClass);
            return false;
        }

        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null) {
            DiscordBot.getLogger().warn("Tried to start {} while not being loaded.", moduleClass);
            return false;
        }

        // Check if all load dependencies are innitlized
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfter()) {
            if (!this.initializedModules.contains(dependencyClass)) {
                DiscordBot.getLogger().warn("Tried to initialize {} without {} dependency being initialized.", moduleClass, dependencyClass);
                return false;
            }
        }

        DiscordBot.getLogger().info("Initialize module {}", module.getName());
        try {
            module.onInitialize();
            this.initializedModules.add(moduleClass);
            return true;
        } catch (final Exception e) {
            DiscordBot.getLogger().error(module.getName(), e);
            Sentry.captureException(e);

            return false;
        }
    }

    public boolean start(final Class<? extends AbstractModule> moduleClass) {
        if (this.startedModules.contains(moduleClass)) {
            DiscordBot.getLogger().warn("Tried to start {} while already being started.", moduleClass);
            return false;
        }

        if (!this.initializedModules.contains(moduleClass)) {
            DiscordBot.getLogger().warn("Tried to start {} while not being initialized.", moduleClass);
            return false;
        }

        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null) {
            DiscordBot.getLogger().warn("Tried to start {} while not being loaded.", moduleClass);
            return false;
        }

        // Check if all load dependencies are started
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfter()) {
            if (!(this.initializedModules.contains(dependencyClass) || this.startedModules.contains(dependencyClass))) {
                DiscordBot.getLogger().warn("Tried to start {} without {} dependency being started", moduleClass, dependencyClass);
                return false;
            }
        }

        DiscordBot.getLogger().info("Starting module {}", module.getName());
        try {
            module.onEnable();
            this.startedModules.add(moduleClass);
            this.initializedModules.remove(moduleClass);
            return true;
        } catch (final Exception e) {
            DiscordBot.getLogger().error(module.getName(), e);
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
            DiscordBot.getLogger().error(module.getName(), e);
            Sentry.captureException(e);

            return false;
        }
    }
}
