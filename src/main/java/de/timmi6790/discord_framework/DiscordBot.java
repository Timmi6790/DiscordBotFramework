package de.timmi6790.discord_framework;

import de.timmi6790.commons.utilities.GsonUtilities;
import de.timmi6790.commons.utilities.ReflectionUtilities;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.ModuleManager;
import io.prometheus.client.cache.caffeine.CacheMetricsCollector;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import io.sentry.Sentry;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.reflections.Reflections;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
@Log4j2
public class DiscordBot {
    public static final String BOT_VERSION = "3.1.1";
    // We need to register it here, because we can only have one global instance of the cache metrics
    public static final CacheMetricsCollector CACHE_METRICS = new CacheMetricsCollector().register();

    private static volatile DiscordBot instance;

    private final ModuleManager moduleManager = new ModuleManager();
    private final Set<AbstractModule> internalModules = new HashSet<>();
    private ShardManager discord;

    public static void main(final String[] args) throws LoginException, TopicalSortCycleException, IOException {
        DiscordBot.getInstance().start();
    }

    public static DiscordBot getInstance() {
        if (instance == null) {
            instance = new DiscordBot();
        }

        return instance;
    }

    @SneakyThrows
    protected Config getConfig() {
        final Path mainConfigPath = Paths.get("./configs/config.json");
        return GsonUtilities.readJsonFile(mainConfigPath, Config.class);
    }

    protected Set<Class<? extends AbstractModule>> getInternalModuleClasses() {
        final Reflections reflections = new Reflections("de.timmi6790.discord_framework.modules");
        return reflections.getSubTypesOf(AbstractModule.class);
    }

    protected void loadInternalModules() {
        // Find all internal modules
        for (final Class<? extends AbstractModule> module : this.getInternalModuleClasses()) {
            try {
                this.internalModules.add(module.getConstructor().newInstance());
            } catch (final Exception e) {
                log.error("Trying to initialize " + module, e);
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DM_EXIT")
    protected boolean setup() throws IOException {
        // Metrics
        DefaultExports.initialize();
        new HTTPServer(8001);

        // Config
        final Path configFolderPath = Paths.get("./configs/");
        Files.createDirectories(configFolderPath);
        final Path configPath = Paths.get(configFolderPath + "/config.json");

        final boolean firstInnit = !Files.exists(configPath);

        final Config config = firstInnit ? new Config() : this.getConfig();
        final Config newConfig = ReflectionUtilities.deepCopy(config);
        for (final AbstractModule module : this.internalModules) {
            newConfig.getEnabledModules().putIfAbsent(module.getModuleName(), Boolean.TRUE);
        }

        GsonUtilities.saveToJsonIfChanged(configPath, config, newConfig);
        if (firstInnit) {
            log.info("Created main config file.");
            return false;
        }

        return true;
    }

    public void start() throws TopicalSortCycleException, LoginException, IOException {
        this.loadInternalModules();
        if (!this.setup()) {
            return;
        }

        final Config mainConfig = this.getConfig();
        if (!mainConfig.getSentry().isEmpty()) {
            Sentry.init(options -> {
                options.setDsn(mainConfig.getSentry());
                options.setRelease(BOT_VERSION);
            });
        }

        // Modules
        for (final AbstractModule module : this.internalModules) {
            if (Boolean.TRUE.equals(mainConfig.getEnabledModules().getOrDefault(module.getModuleName(), Boolean.TRUE))) {
                this.moduleManager.registerModule(module);
            }
        }
        this.moduleManager.loadExternalModules();

        // Discord
        final Set<GatewayIntent> requiredGatewayIntents = new HashSet<>();
        for (final AbstractModule loadedModule : this.moduleManager.getLoadedModules().values()) {
            requiredGatewayIntents.addAll(loadedModule.getRequiredGatewayIntents());
        }

        log.debug("Starting discord with {} gateway intents.", requiredGatewayIntents);
        this.discord = DefaultShardManagerBuilder.createLight(mainConfig.getDiscordToken(), requiredGatewayIntents)
                .setStatus(OnlineStatus.ONLINE)
                .build();

        log.debug("Initialize all modules");
        this.moduleManager.initializeAll();
        log.debug("Await discord ready");
        log.debug("Start all modules");
        // Delay the module start by 2 seconds to await discord
        // This is a temporary fix, because the awaitReady method got the bot stuck lately
        Executors.newSingleThreadScheduledExecutor().schedule(
                this.moduleManager::startAll,
                2, TimeUnit.SECONDS
        );
        log.debug("Done starting all modules");
    }

    public JDA getBaseShard() {
        return this.discord.getShardById(0);
    }
}
