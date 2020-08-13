package de.timmi6790.discord_framework.modules;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.exceptions.ModuleGetException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.utilities.sorting.TopicalSort;
import lombok.Data;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Data
public class ModuleManager {
    private final Map<Class<? extends AbstractModule>, AbstractModule> loadedModules = new HashMap<>();
    private final Set<Class<? extends AbstractModule>> startedModules = new HashSet<>();

    public boolean registerModule(final AbstractModule module) {
        if (this.loadedModules.containsKey(module.getClass())) {
            return false;
        }

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

    private Set<AbstractModule> getExternalModules() {
        final Set<AbstractModule> abstractModules = new HashSet<>();

        final File[] pluginJars = new File(DiscordBot.getBasePath().toString() + "/plugins/").listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        for (final File jar : pluginJars) {
            DiscordBot.getLogger().info("Checking {} for modules.", jar.getName());
            try (final JarFile jarFile = new JarFile(jar)) {
                final URL jarUrl = jar.toURI().toURL();
                try (final URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, ClassLoader.getSystemClassLoader())) {
                    final URL pluginUrl = classLoader.getResource("plugin.json");
                    if (pluginUrl == null) {
                        DiscordBot.getLogger().warn("Can't load {}, no plugins.json found.", jar.getName());
                        continue;
                    }

                    final Plugin plugin = DiscordBot.getGson().fromJson(new InputStreamReader(pluginUrl.openStream()), Plugin.class);
                    for (final String path : plugin.getModules()) {
                        final Class<?> pluginClass;
                        try {
                            pluginClass = classLoader.loadClass(path);
                        } catch (final ClassNotFoundException ignore) {
                            DiscordBot.getLogger().warn("Can't load Module {} inside {}, unknown path.", path, jar.getName());
                            continue;
                        }
                        if (!AbstractModule.class.isAssignableFrom(pluginClass)) {
                            DiscordBot.getLogger().warn("Module {} inside {} is not extending AbstractModule!", path, jar.getName());
                            continue;
                        }

                        final Class<?> clazz = Class.forName(path, true, classLoader);
                        final Class<? extends AbstractModule> newClass = clazz.asSubclass(AbstractModule.class);
                        final Constructor<? extends AbstractModule> constructor = newClass.getConstructor();
                        final AbstractModule pluginModule = constructor.newInstance();

                        if (abstractModules.contains(pluginModule)) {
                            DiscordBot.getLogger().warn("Module {} inside {} is already loaded.", pluginModule.getName(), jar.getName());
                            continue;
                        }

                        // Load all module classes
                        final Enumeration<JarEntry> entries = jarFile.entries();
                        for (JarEntry entry = entries.nextElement(); entries.hasMoreElements(); entry = entries.nextElement()) {
                            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                                continue;
                            }

                            String className = entry.getName().substring(0, entry.getName().length() - 6);
                            className = className.replace('/', '.');
                            classLoader.loadClass(className);
                        }

                        abstractModules.add(pluginModule);
                    }
                }
            } catch (final Exception e) {
                DiscordBot.getLogger().warn(e, "Error while trying to load modules from {}.", jar.getName());
            }
        }

        return abstractModules;
    }

    public boolean start(final Class<? extends AbstractModule> moduleClass) {
        final AbstractModule module = this.loadedModules.get(moduleClass);
        if (module == null || this.startedModules.contains(moduleClass)) {
            return false;
        }

        // Check if all load dependencies are started
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfter()) {
            if (!this.startedModules.contains(dependencyClass)) {
                DiscordBot.getLogger().warn("Tried to start {} without {} dependency being started", moduleClass, dependencyClass);
                return false;
            }
        }

        DiscordBot.getLogger().info("Starting module {}", module.getName());
        try {
            module.onEnable();
            this.startedModules.add(moduleClass);
            return true;
        } catch (final Exception e) {
            DiscordBot.getLogger().error(module.getName(), e);
            DiscordBot.getSentry().sendException(e);

            return false;
        }
    }

    public void startAll() throws TopicalSortCycleException {
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
        moduleSort.sort().forEach(this::start);
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
            DiscordBot.getSentry().sendException(e);
            return false;
        }
    }
}
