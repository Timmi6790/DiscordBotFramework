package de.timmi6790.discord_framework.modules.new_module_manager.dpi;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider.DependencyProvider;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider.providers.ModuleDependencyProvider;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider.providers.ServiceDependencyProvider;
import de.timmi6790.discord_framework.utilities.TopicalSort;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class DependencyManager {
    private final Map<Class<?>, Object> dependencies = new ConcurrentHashMap<>();

    private final Map<Class<?>, DependencyInfo[]> dependencyInfos = new ConcurrentHashMap<>();

    private final List<DependencyProvider> providers = new ArrayList<>();

    public DependencyManager() {
        Collections.addAll(
                this.providers,
                new ModuleDependencyProvider(),
                new ServiceDependencyProvider()
        );
    }

    private boolean hasOptionalAnnotation(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation instanceof OptionalDependency) {
                return true;
            }
        }
        return false;
    }

    private List<Class<?>> getLoadOrder() throws TopicalSortCycleException {
        final List<DependencyInfo[]> modules = new ArrayList<>(this.dependencyInfos.values());
        final List<Class<?>> moduleClasses = new ArrayList<>(this.dependencyInfos.keySet());

        // Sort modules after dependency order
        final List<TopicalSort.Dependency> edges = new ArrayList<>();
        for (int index = 0; modules.size() > index; index++) {
            final DependencyInfo[] module = modules.get(index);

            for (final DependencyInfo dependencyInfo : module) {
                final int loadIndex = moduleClasses.indexOf(dependencyInfo.getDependency());
                if (loadIndex != -1) {
                    edges.add(new TopicalSort.Dependency(index, loadIndex));
                }
            }
        }

        final TopicalSort<Class<?>> moduleSort = new TopicalSort<>(moduleClasses, edges);
        return moduleSort.sort();
    }

    private Object getDependency(final DependencyInfo dependencyInfo) {
        final Class<?> dependencyClass = dependencyInfo.getDependency();

        final Optional<Object> dependency = this.getDependency(dependencyClass);
        if (dependency.isPresent()) {
            return dependency.get();
        } else if (dependencyInfo.isOptional()) {
            return null;
        } else {
            throw new RuntimeException("Can't find required dependency " + dependencyClass);
        }
    }

    private Constructor<?> getConstructor(final Class<?> clazz) {
        return clazz.getConstructors()[0];
    }

    public boolean hasDependency(final Class<?> dependencyClass) {
        return this.dependencies.containsKey(dependencyClass);
    }

    public void loadModules() {
        for (final DependencyProvider provider : this.providers) {
            log.info("Loading modules from {}Provider", provider.getName());
            for (final Class<?> dependency : provider.getDependencies()) {
                this.addDependency(dependency);
            }
        }
    }

    public void initialize(final Class<?> dependencyClass) {
        final DependencyInfo[] dependencyInfos = this.dependencyInfos.get(dependencyClass);
        final Constructor<?> constructor = this.getConstructor(dependencyClass);

        final Object[] constructorParameters = new Object[dependencyInfos.length];
        for (int index = 0; constructorParameters.length > index; index++) {
            final DependencyInfo dependencyInfo = dependencyInfos[index];
            constructorParameters[index] = this.getDependency(dependencyInfo);
        }

        final Object newInstance;
        try {
            newInstance = constructor.newInstance(constructorParameters);

        } catch (final IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("", e);
            throw new IllegalStateException("Module initialization error");
        }

        this.dependencies.put(dependencyClass, newInstance);
    }

    public void initializeAll() {
        try {
            for (final Class<?> moduleClazz : this.getLoadOrder()) {
                log.debug("Initializing dependency {}", moduleClazz);
                this.initialize(moduleClazz);
            }
        } catch (final TopicalSortCycleException e) {
            throw new RuntimeException("Circular dependency found");
        }
    }

    public void addDependency(final Class<?> dependencyClass) {
        log.debug("Add dependency {}", dependencyClass);

        // Get dependencies
        final Constructor<?> constructor = this.getConstructor(dependencyClass);
        final Annotation[][] constructorParameterAnnotations = constructor.getParameterAnnotations();
        final Class<?>[] parameters = constructor.getParameterTypes();

        final DependencyInfo[] dependencies = new DependencyInfo[parameters.length];
        for (int index = 0; parameters.length > index; index++) {
            final Class<?> parameter = parameters[index];
            final boolean isOptional = this.hasOptionalAnnotation(constructorParameterAnnotations[index]);
            dependencies[index] = new DependencyInfo(
                    parameter,
                    isOptional
            );
        }

        this.dependencyInfos.put(
                dependencyClass,
                dependencies
        );
    }

    public void addDependency(final Object dependency) {
        this.dependencies.put(dependency.getClass(), dependency);
    }

    public void addDependencies(final Class<?>... dependencyClasses) {
        for (final Class<?> dependencyClass : dependencyClasses) {
            this.addDependency(dependencyClass);
        }
    }

    public Optional<Object> getDependency(final Class<?> dependencyClass) {
        return Optional.ofNullable(this.dependencies.get(dependencyClass));
    }
}
