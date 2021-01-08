package de.timmi6790.discord_framework.modules;


import de.timmi6790.discord_framework.DiscordBot;
import lombok.Data;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.*;

/**
 * The base of every module
 */
@Data
public abstract class AbstractModule {
    private final String moduleName;
    private Set<Class<? extends AbstractModule>> dependencies = new HashSet<>();
    private Set<Class<? extends AbstractModule>> loadAfterDependencies = new HashSet<>();
    private Set<Class<? extends AbstractModule>> loadBeforeDependencies = new HashSet<>();
    private Set<GatewayIntent> requiredGatewayIntents = EnumSet.noneOf(GatewayIntent.class);

    /**
     * Instantiates a new module.
     *
     * @param moduleName the module name
     */
    protected AbstractModule(final String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Called on module initialization(Pre discord await). This should be used to setup dependencies between the
     * modules.
     */
    public void onInitialize() {
    }

    /**
     * Called on module enable(Post discord await). This should be used to setup everything related to discord
     */
    public void onEnable() {
    }

    /**
     * On disable.
     */
    public void onDisable() {
    }

    /**
     * Generate internal name string.
     *
     * @param module       the module
     * @param categoryName the category name
     * @param valueName    the value name
     * @return the string
     */
    protected String generateInternalName(final AbstractModule module, final String categoryName, final String valueName) {
        return String.format("%s.%s.%s", module.getModuleName(), categoryName, valueName)
                .replace(' ', '_')
                .toLowerCase();
    }

    /**
     * Add discord gateway intents required for the module to work
     *
     * @param gatewayIntents the discord gateway intents
     */
    protected final void addDiscordGatewayIntents(final GatewayIntent... gatewayIntents) {
        this.requiredGatewayIntents.addAll(Arrays.asList(gatewayIntents));
    }

    /**
     * Add dependencies.
     *
     * @param dependencies the dependencies
     */
    @SafeVarargs
    protected final void addDependencies(final Class<? extends AbstractModule>... dependencies) {
        this.dependencies.addAll(Arrays.asList(dependencies));
    }

    /**
     * Add dependencies and load after.
     *
     * @param dependencies the dependencies
     */
    @SafeVarargs
    protected final void addDependenciesAndLoadAfter(final Class<? extends AbstractModule>... dependencies) {
        this.addDependencies(dependencies);
        this.addLoadAfterDependencies(dependencies);
    }

    /**
     * Add load after dependencies.
     *
     * @param loadAfter the load after
     */
    @SafeVarargs
    protected final void addLoadAfterDependencies(final Class<? extends AbstractModule>... loadAfter) {
        this.loadAfterDependencies.addAll(Arrays.asList(loadAfter));
    }

    /**
     * Add load before dependencies.
     *
     * @param loadBefore the load before
     */
    @SafeVarargs
    protected final void addLoadBeforeDependencies(final Class<? extends AbstractModule>... loadBefore) {
        this.loadBeforeDependencies.addAll(Arrays.asList(loadBefore));
    }

    /**
     * Get module by class
     *
     * @param <T>   the module class
     * @param clazz the module class
     * @return the module
     */
    public <T extends AbstractModule> Optional<T> getModule(final Class<T> clazz) {
        return this.getModuleManager().getModule(clazz);
    }

    /**
     * Get module by class or throw
     *
     * @param <T>   the module class
     * @param clazz the module class
     * @return the module
     */
    public <T extends AbstractModule> T getModuleOrThrow(final Class<T> clazz) {
        return this.getModuleManager().getModuleOrThrow(clazz);
    }

    /**
     * Gets discord bot.
     *
     * @return the discord bot
     */
    public DiscordBot getDiscordBot() {
        return DiscordBot.getInstance();
    }

    /**
     * Gets discord.
     *
     * @return the discord
     */
    public ShardManager getDiscord() {
        return this.getDiscordBot().getDiscord();
    }

    /**
     * Gets module manager.
     *
     * @return the module manager
     */
    public ModuleManager getModuleManager() {
        return this.getDiscordBot().getModuleManager();
    }
}
