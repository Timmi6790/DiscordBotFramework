package de.timmi6790.discord_framework.module.provider.providers;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.provider.ModuleProvider;
import org.reflections.Reflections;

import java.util.Collection;

public class InternalModuleProvider implements ModuleProvider {
    @Override
    public String getName() {
        return "Internal";
    }

    @Override
    public Collection<Class<? extends AbstractModule>> getModules() {
        final Reflections reflections = new Reflections("de.timmi6790.discord_framework.module.modules");
        return reflections.getSubTypesOf(AbstractModule.class);
    }
}
