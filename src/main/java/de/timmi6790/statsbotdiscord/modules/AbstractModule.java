package de.timmi6790.statsbotdiscord.modules;


import lombok.Data;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

@Data
public abstract class AbstractModule {
    private final String name;
    private Set<Class<? extends AbstractModule>> dependencies = new HashSet<>();
    private Set<Class<? extends AbstractModule>> loadAfter = new HashSet<>();
    private Set<Class<? extends AbstractModule>> loadBefore = new HashSet<>();

    public AbstractModule(final String name) {
        this.name = name;
    }

    protected final void addDependencies(final Class<? extends AbstractModule>... dependencies) {
        this.dependencies.addAll(Arrays.asList(dependencies));
    }

    protected final void addDependenciesAndLoadAfter(final Class<? extends AbstractModule>... dependencies) {
        this.addDependencies(dependencies);
        this.addLoadAfter(dependencies);
    }

    protected final void addLoadAfter(final Class<? extends AbstractModule>... loadAfter) {
        this.loadAfter.addAll(Arrays.asList(loadAfter));
    }

    protected final void addLoadBefore(final Class<? extends AbstractModule>... loadBefore) {
        this.loadBefore.addAll(Arrays.asList(loadBefore));
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
