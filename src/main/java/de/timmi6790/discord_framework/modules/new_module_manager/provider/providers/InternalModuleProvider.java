package de.timmi6790.discord_framework.modules.new_module_manager.provider.providers;

import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.new_module_manager.provider.ModuleProvider;
import org.reflections.Reflections;

import java.util.Collection;

public class InternalModuleProvider implements ModuleProvider {
    private static final String MODULE_PATH = "de.timmi6790.discord_framework.modules";

    @Override
    public String getName() {
        return "Internal";
    }

    @Override
    public Collection<Class<? extends Module>> getModules() {
        final Reflections reflections = new Reflections(MODULE_PATH);
        return reflections.getSubTypesOf(Module.class);
    }
}
