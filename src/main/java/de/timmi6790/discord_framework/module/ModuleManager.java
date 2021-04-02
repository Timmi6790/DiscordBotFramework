package de.timmi6790.discord_framework.module;

import com.google.common.collect.Lists;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.module.exceptions.ModuleNotFoundException;
import de.timmi6790.discord_framework.module.exceptions.ModuleUninitializedException;
import de.timmi6790.discord_framework.module.provider.ModuleProvider;
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
// TODO: Implement config
public class ModuleManager {
    private final List<ModuleProvider> providers = new ArrayList<>();
    private final Map<Class<? extends AbstractModule>, ModuleInfo> modules = new HashMap<>();

    private boolean hasDependency(final Class<? extends AbstractModule> module,
                                  final Class<? extends AbstractModule> dependency,
                                  final ModuleStatus requiredDependencyStatus) {
        final Optional<ModuleInfo> dependencyModuleInfoOpt = this.getModuleInfo(dependency);
        if (!dependencyModuleInfoOpt.isPresent()) {
            log.warn(
                    "Dependency {} for {} is not registered.",
                    dependency,
                    module
            );
            return false;
        }

        final ModuleStatus dependencyStatus = dependencyModuleInfoOpt.get().getStatus();
        if (dependencyStatus == requiredDependencyStatus) {
            log.warn(
                    "Dependency {} for {} has invalid status of {}",
                    dependency,
                    module,
                    dependencyStatus
            );
            return false;
        }

        return true;
    }

    private List<Class<? extends AbstractModule>> getSortedModules(final ModuleStatus moduleStatus) throws TopicalSortCycleException {
        final List<ModuleInfo> moduleInfos = this.getModuleInfos(moduleStatus);

        final List<AbstractModule> modules = Lists.newArrayListWithCapacity(moduleInfos.size());
        final List<Class<? extends AbstractModule>> moduleClasses = Lists.newArrayListWithCapacity(moduleInfos.size());
        for (final ModuleInfo moduleInfo : moduleInfos) {
            modules.add(moduleInfo.getModule());
            moduleClasses.add(moduleInfo.getModuleClass());
        }

        // Sort modules after load order
        final List<TopicalSort.Dependency> edges = new ArrayList<>();
        for (int moduleIndex = 0; modules.size() > moduleIndex; moduleIndex++) {
            final AbstractModule module = modules.get(moduleIndex);

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

    public void registerModules(final Class<? extends AbstractModule>... moduleClasses) {
        for (final Class<? extends AbstractModule> moduleClass : moduleClasses) {
            this.registerModule(moduleClass);
        }
    }

    public void registerModules(final Collection<Class<? extends AbstractModule>> moduleClasses) {
        for (final Class<? extends AbstractModule> moduleClass : moduleClasses) {
            this.registerModule(moduleClass);
        }
    }

    public List<ModuleInfo> getModuleInfos(final ModuleStatus status) {
        final List<ModuleInfo> modules = Lists.newArrayListWithExpectedSize(this.modules.size());

        for (final ModuleInfo moduleInfo : this.modules.values()) {
            if (moduleInfo.getStatus() == status) {
                modules.add(moduleInfo);
            }
        }

        return modules;
    }

    public Optional<ModuleInfo> getModuleInfo(final Class<? extends AbstractModule> clazz) {
        return Optional.ofNullable(this.modules.get(clazz));
    }

    public List<AbstractModule> getModules(final ModuleStatus status) {
        final List<ModuleInfo> moduleInfos = this.getModuleInfos(status);
        final List<AbstractModule> modules = Lists.newArrayListWithExpectedSize(moduleInfos.size());

        for (final ModuleInfo moduleInfo : moduleInfos) {
            modules.add(moduleInfo.getModule());
        }
        return modules;
    }

    public boolean registerModule(final Class<? extends AbstractModule> moduleClass) {
        if (this.modules.containsKey(moduleClass)) {
            log.debug("{} is already registered!", moduleClass);
            return false;
        }

        final AbstractModule module;
        try {
            module = moduleClass
                    .asSubclass(AbstractModule.class)
                    .getConstructor()
                    .newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            Sentry.captureException(e);
            return false;
        }

        log.debug("Registered {}[{}]", module.getModuleName(), moduleClass);
        this.modules.put(
                moduleClass,
                new ModuleInfo(
                        moduleClass,
                        module
                )
        );
        return true;
    }

    public <T extends AbstractModule> Optional<T> getModule(final Class<T> clazz) {
        return (Optional<T>) this.getModuleInfo(clazz).map(ModuleInfo::getModule);
    }

    public <T extends AbstractModule> T getModuleOrThrow(final Class<T> clazz) {
        return this.getModule(clazz).orElseThrow(() -> new ModuleNotFoundException(String.valueOf(clazz)));
    }

    public boolean initialize(final Class<? extends AbstractModule> moduleClass) {
        final Optional<ModuleInfo> moduleInfoOpt = this.getModuleInfo(moduleClass);
        if (!moduleInfoOpt.isPresent()) {
            log.warn("The module {} is not registered!", moduleClass);
            return false;
        }

        final ModuleInfo moduleInfo = moduleInfoOpt.get();
        if (moduleInfo.getStatus() != ModuleStatus.REGISTERED) {
            log.warn(
                    "Tried to initialize {} while invalid status of {} found. " +
                            "You can only initialize modules with the {} status.",
                    moduleClass,
                    moduleInfo.getStatus(),
                    ModuleStatus.REGISTERED
            );
            return false;
        }

        final AbstractModule module;
        try {
            module = moduleInfo.getModule();
        } catch (final ModuleUninitializedException e) {
            log.warn("Tried to start {} while not being loaded.", moduleClass);
            return false;
        }

        // Check if all load dependencies are registered
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfterDependencies()) {
            if (!this.hasDependency(moduleClass, dependencyClass, ModuleStatus.REGISTERED)) {
                return false;
            }
        }

        log.info("Initialize module {}", module.getModuleName());
        try {
            final boolean initializeStatus = module.onInitialize();
            if (initializeStatus) {
                moduleInfo.setStatus(ModuleStatus.INITIALIZED);
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
        final Optional<ModuleInfo> moduleInfoOpt = this.getModuleInfo(moduleClass);
        if (!moduleInfoOpt.isPresent()) {
            log.warn("The module {} is not registered!", moduleClass);
            return false;
        }

        final ModuleInfo moduleInfo = moduleInfoOpt.get();
        if (moduleInfo.getStatus() != ModuleStatus.INITIALIZED) {
            log.warn(
                    "Tried to start {} while invalid status of {} found. " +
                            "You can only initialize modules with the {} status.",
                    moduleClass,
                    moduleInfo.getStatus(),
                    ModuleStatus.INITIALIZED
            );
            return false;
        }

        final AbstractModule module = moduleInfo.getModule();
        // Check if all load dependencies are initialized
        for (final Class<? extends AbstractModule> dependencyClass : module.getLoadAfterDependencies()) {
            if (!this.hasDependency(moduleClass, dependencyClass, ModuleStatus.INITIALIZED)) {
                return false;
            }
        }

        log.info("Starting module {}", module.getModuleName());
        try {
            final boolean enableStatus = module.onEnable();
            if (enableStatus) {
                moduleInfo.setStatus(ModuleStatus.STARTED);
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
        for (final Class<? extends AbstractModule> moduleClass : this.getSortedModules(ModuleStatus.REGISTERED)) {
            this.initialize(moduleClass);
        }
    }

    @SneakyThrows
    public void startAll() {
        for (final Class<? extends AbstractModule> moduleClass : this.getSortedModules(ModuleStatus.INITIALIZED)) {
            this.start(moduleClass);
        }
    }

    public void addModuleProviders(final ModuleProvider... moduleProviders) {
        for (final ModuleProvider moduleProvider : moduleProviders) {
            this.addModuleProvider(moduleProvider);
        }
    }

    public void addModuleProvider(final ModuleProvider moduleProvider) {
        this.providers.add(moduleProvider);
    }

    public void loadModules() {
        for (final ModuleProvider moduleProvider : this.providers) {
            log.info("Load modules from {}-Provider", moduleProvider.getName());
            final Collection<Class<? extends AbstractModule>> foundModules = moduleProvider.getModules();
            log.info("Found {} modules from {}-Provider", foundModules.size(), moduleProvider.getName());
            this.registerModules(foundModules);
        }
    }

    public boolean stopModule(final Class<? extends AbstractModule> moduleClass) {
        final Optional<ModuleInfo> moduleInfoOpt = this.getModuleInfo(moduleClass);
        if (!moduleInfoOpt.isPresent() || moduleInfoOpt.get().getStatus() == ModuleStatus.REGISTERED) {
            return false;
        }

        final ModuleInfo moduleInfo = moduleInfoOpt.get();
        log.info("Stopping module {}", moduleInfo.getModule().getModuleName());
        try {
            moduleInfo.getModule().onDisable();
            moduleInfo.setStatus(ModuleStatus.REGISTERED);
            return true;
        } catch (final Exception e) {
            log.error(moduleInfo.getModule().getModuleName(), e);
            Sentry.captureException(e);

            return false;
        }
    }
}
