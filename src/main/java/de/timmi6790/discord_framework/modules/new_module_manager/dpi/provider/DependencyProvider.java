package de.timmi6790.discord_framework.modules.new_module_manager.dpi.provider;

import java.util.Collection;

public interface DependencyProvider {
    String getName();

    Collection<Class<?>> getDependencies();
}
