package de.timmi6790.discord_framework.modules;


import de.timmi6790.discord_framework.DiscordBot;
import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.*;

@Data
public abstract class AbstractModule {
    private final String name;
    private Set<Class<? extends AbstractModule>> dependencies = new HashSet<>();
    private Set<Class<? extends AbstractModule>> loadAfter = new HashSet<>();
    private Set<Class<? extends AbstractModule>> loadBefore = new HashSet<>();
    private Set<GatewayIntent> requiredGatewayIntents = EnumSet.noneOf(GatewayIntent.class);

    protected AbstractModule(final String name) {
        this.name = name;
    }

    public void onInitialize() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    protected final void addGatewayIntents(final GatewayIntent... gatewayIntents) {
        this.requiredGatewayIntents.addAll(Arrays.asList(gatewayIntents));
    }

    @SafeVarargs
    protected final void addDependencies(final Class<? extends AbstractModule>... dependencies) {
        this.dependencies.addAll(Arrays.asList(dependencies));
    }

    @SafeVarargs
    protected final void addDependenciesAndLoadAfter(final Class<? extends AbstractModule>... dependencies) {
        this.addDependencies(dependencies);
        this.addLoadAfter(dependencies);
    }

    @SafeVarargs
    protected final void addLoadAfter(final Class<? extends AbstractModule>... loadAfter) {
        this.loadAfter.addAll(Arrays.asList(loadAfter));
    }

    @SafeVarargs
    protected final void addLoadBefore(final Class<? extends AbstractModule>... loadBefore) {
        this.loadBefore.addAll(Arrays.asList(loadBefore));
    }

    public <T extends AbstractModule> Optional<T> getModule(final Class<T> clazz) {
        return this.getModuleManager().getModule(clazz);
    }

    public <T extends AbstractModule> T getModuleOrThrow(final Class<T> clazz) {
        return this.getModuleManager().getModuleOrThrow(clazz);
    }

    public DiscordBot getDiscordBot() {
        return DiscordBot.getInstance();
    }

    public JDA getDiscord() {
        return this.getDiscordBot().getDiscord();
    }

    public ModuleManager getModuleManager() {
        return this.getDiscordBot().getModuleManager();
    }
}
