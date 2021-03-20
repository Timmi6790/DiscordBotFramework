package de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider.providers;

import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider.DependencyProvider;
import org.reflections.Reflections;

import java.util.Collection;
import java.util.HashSet;

public class ModuleDependencyProvider implements DependencyProvider {
    private static final String MODULE_PATH = "de.timmi6790.discord_framework.modules";

    @Override
    public String getName() {
        return "Module";
    }

    @Override
    public Collection<Class<?>> getDependencies() {
        final Reflections reflections = new Reflections(MODULE_PATH);
        return new HashSet<>(reflections.getSubTypesOf(Module.class));
    }
}
