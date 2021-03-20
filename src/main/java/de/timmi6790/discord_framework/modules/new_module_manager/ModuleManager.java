package de.timmi6790.discord_framework.modules.new_module_manager;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.DependencyManager;
import de.timmi6790.discord_framework.modules.new_module_manager.provider.ModuleProvider;
import de.timmi6790.discord_framework.modules.new_module_manager.provider.providers.InternalModuleProvider;
import de.timmi6790.discord_framework.modules.new_module_manager.provider.providers.jar.JarModuleProvider;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
// TODO: Optimize Exceptions
// TODO: Add config, to disable modules
// TODO: Add provider configs
public class ModuleManager {
    private final DependencyManager dependencyManager;
    private final List<ModuleProvider> moduleProviders = new ArrayList<>();
    private final Map<Class<? extends Module>, ModuleInfo> modules = new ConcurrentHashMap<>();

    public static void main(final String[] args) {
        new ModuleManager(DiscordBot.getInstance())
                .loadModules()
                .onDiscordReady(null);
    }

    public ModuleManager(final DiscordBot discordBot) {
        this.dependencyManager = new DependencyManager();

        this.dependencyManager.addDependency(discordBot);

        this.dependencyManager.loadModules();
        this.dependencyManager.initializeAll();

        this.addModuleProviders(
                new InternalModuleProvider(),
                new JarModuleProvider()
        );
    }

    private void addModule(final Class<? extends Module> clazz) {
        log.info("Adding module {}", clazz);
        if (clazz.getConstructors().length > 1) {
            throw new RuntimeException("Invalid module " + clazz + "Found more than 1 constructor");
        }

        this.modules.put(
                clazz,
                new ModuleInfo()
        );
    }

    public void addModuleProviders(final ModuleProvider... moduleProviders) {
        this.moduleProviders.addAll(Arrays.asList(moduleProviders));
    }

    public Optional<ModuleInfo> getModuleInfo(final Class<?> clazz) {
        return Optional.ofNullable(this.modules.get(clazz));
    }

    public List<ModuleInfo> getModuleInfos(final ModuleStatus moduleStatus) {
        final List<ModuleInfo> moduleInfos = new ArrayList<>();

        for (final ModuleInfo info : this.modules.values()) {
            if (info.getStatus() == moduleStatus) {
                moduleInfos.add(info);
            }
        }

        return moduleInfos;
    }

    public <T> Optional<T> getModule(final Class<T> clazz) {
        return (Optional<T>) this.dependencyManager.getDependency(clazz);
    }

    public <T> T getModuleOrThrow(final Class<T> clazz) {
        return this.getModule(clazz).orElseThrow(RuntimeException::new);
    }

    public ModuleManager loadModules() {
        for (final ModuleProvider moduleProvider : this.moduleProviders) {
            log.info("Loading modules from {}Provider", moduleProvider.getName());
            for (final Class<? extends Module> moduleClass : moduleProvider.getModules()) {
                this.addModule(moduleClass);
            }
        }
        return this;
    }

    private void initialize(final Class<?> moduleClazz) {

    }

    public void onDiscordReady(final ShardManager shardManager) {
        for (final ModuleInfo moduleInfo : this.getModuleInfos(ModuleStatus.STARTED)) {
            moduleInfo.getModule().onDiscordReady(shardManager);
        }
    }

    public Set<GatewayIntent> getGatewayIntents() {
        final Set<GatewayIntent> gatewayIntents = EnumSet.noneOf(GatewayIntent.class);

        for (final ModuleInfo moduleInfo : this.getModuleInfos(ModuleStatus.STARTED)) {
            gatewayIntents.addAll(Arrays.asList(moduleInfo.getGatewayIntents()));
        }

        return gatewayIntents;
    }
}
