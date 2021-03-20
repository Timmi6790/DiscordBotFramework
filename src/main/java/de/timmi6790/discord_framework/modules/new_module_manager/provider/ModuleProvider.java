package de.timmi6790.discord_framework.modules.new_module_manager.provider;

import de.timmi6790.discord_framework.modules.new_module_manager.Module;

import java.util.Collection;

public interface ModuleProvider {
    String getName();

    Collection<Class<? extends Module>> getModules();
}
