package de.timmi6790.discord_framework.modules;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.exceptions.ModuleGetException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.utilities.sorting.TopicalSort;
import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        final AtomicInteger index = new AtomicInteger(0);
        modules.forEach(module -> {
            final int moduleIndex = index.getAndIncrement();

            // Load after
            module.getLoadAfter()
                    .parallelStream()
                    .map(moduleClasses::indexOf)
                    .filter(dependencyIndex -> dependencyIndex != -1)
                    .map(dependencyIndex -> new TopicalSort.Dependency(moduleIndex, dependencyIndex))
                    .forEach(edges::add);

            // Load before
            module.getLoadBefore()
                    .parallelStream()
                    .map(moduleClasses::indexOf)
                    .filter(dependencyIndex -> dependencyIndex != -1)
                    .map(dependencyIndex -> new TopicalSort.Dependency(dependencyIndex, moduleIndex))
                    .forEach(edges::add);
        });

        final TopicalSort<Class<? extends AbstractModule>> moduleSort = new TopicalSort<>(moduleClasses, edges);
        moduleSort.sort().forEach(this::start);
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
