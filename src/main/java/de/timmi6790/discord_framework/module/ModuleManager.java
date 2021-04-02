package de.timmi6790.discord_framework.module;

import de.timmi6790.discord_framework.exceptions.ModuleNotFoundException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.module.provider.providers.jar.JarModuleProvider;
import de.timmi6790.discord_framework.utilities.TopicalSort;
import io.sentry.Sentry;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Global module manager. All models are registered,initialized and started over this manager.
 */
@Data
@Log4j2
public class ModuleManager {
    private final Map<Class<? extends AbstractModule>, AbstractModule> loadedModules = new HashMap<>();
    private final Set<Class<? extends AbstractModule>> initializedModules = new HashSet<>();
    private final Set<Class<? extends AbstractModule>> startedModules = new HashSet<>();

    private List<Class<? extends AbstractModule>> getSortedModules() throws TopicalSortCycleException {
        final List<AbstractModule> modules = new ArrayList<>(this.loadedModules.values());
        final List<Class<? extends AbstractModule>> moduleClasses = new ArrayList<>(this.loadedModules.keySet());

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
        }

        final TopicalSort<Class<? extends AbstractModule>> moduleSort = new TopicalSort<>(moduleClasses, edges);
        return moduleSort.sort();
    }

    private Set<AbstractModule> getExternalModules() {
        final Set<AbstractModule> abstractModules = new HashSet<>();

        final JarModuleProvider jarModuleProvider = new JarModuleProvider();
        for (final Class<? extends AbstractModule> moduleClass : jarModuleProvider.getModules()) {
            try {
                log.info(
                        "Creating new instance of {}",
                        moduleClass
                );
                final AbstractModule module = moduleClass
                        .asSubclass(AbstractModule.class)
                        .getConstructor()
                        .newInstance();
                abstractModules.add(module);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error(
                        "Execution while trying to create new instance of " + moduleClass,
                        e
                );
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
