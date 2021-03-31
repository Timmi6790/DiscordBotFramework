package de.timmi6790.discord_framework.module.provider;

import de.timmi6790.discord_framework.module.AbstractModule;

import java.util.Collection;

public interface ModuleProvider {
    String getName();

    Collection<Class<? extends AbstractModule>> getModules();
}
