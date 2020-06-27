package de.timmi6790.statsbotdiscord.modules;


import lombok.Data;

@Data
public abstract class AbstractModule {
    private final String name;
    
    public abstract void onEnable();

    public abstract void onDisable();
}
