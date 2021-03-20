package de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider.providers;

import de.timmi6790.discord_framework.modules.new_module_manager.dpi.Service;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider.DependencyProvider;
import org.reflections.Reflections;

import java.util.Collection;

public class ServiceDependencyProvider implements DependencyProvider {
    @Override
    public String getName() {
        return "Service";
    }

    @Override
    public Collection<Class<?>> getDependencies() {
        final Reflections reflections = new Reflections("");
        return reflections.getTypesAnnotatedWith(Service.class);
    }
}
