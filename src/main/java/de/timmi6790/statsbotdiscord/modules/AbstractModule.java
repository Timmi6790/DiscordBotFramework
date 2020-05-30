package de.timmi6790.statsbotdiscord.modules;


import lombok.Getter;

public abstract class AbstractModule {
    @Getter
    private final String name;

    public AbstractModule(final String name) {
        this.name = name;
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
