package de.timmi6790.discord_framework;

import de.timmi6790.commons.utilities.GsonUtilities;
import de.timmi6790.commons.utilities.ReflectionUtilities;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.new_module_manager.ModuleManager;
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

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
@Log4j2
public class DiscordBot {
    public static final String BOT_VERSION = "3.1.2";
    // We need to register it here, because we can only have one global instance of the cache metrics
    public static final CacheMetricsCollector CACHE_METRICS = new CacheMetricsCollector().register();

    private static final DiscordBot INSTANCE = new DiscordBot();

    private final ModuleManager moduleManager;
    private ShardManager discord;

    public static void main(final String[] args) throws LoginException, TopicalSortCycleException, IOException {
        DiscordBot.getInstance().start();
    }

    public DiscordBot() {
        this.moduleManager = new ModuleManager(this);
    }

    public static DiscordBot getInstance() {
        return INSTANCE;
    }

    @SneakyThrows
    protected Config getConfig() {
        final Path mainConfigPath = Paths.get("./configs/config.json");
        return GsonUtilities.readJsonFile(mainConfigPath, Config.class);
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

        GsonUtilities.saveToJsonIfChanged(configPath, config, newConfig);
        if (firstInnit) {
            log.info("Created main config file.");
            return false;
        }

        return true;
    }

    public void start() throws LoginException, IOException {
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

        // Model Manager
        this.moduleManager
                .loadModules();

        // Discord
        final Set<GatewayIntent> requiredGatewayIntents = this.moduleManager.getGatewayIntents();
        log.debug("Starting discord with {} gateway intents.", requiredGatewayIntents);
        this.discord = DefaultShardManagerBuilder.createLight(mainConfig.getDiscordToken(), requiredGatewayIntents)
                .setStatus(OnlineStatus.ONLINE)
                .build();

        log.debug("Initialize all modules");
        // this.moduleManager.initializeAll();
        log.debug("Await discord ready");
        log.debug("Start all modules");
        // Delay the module start by 2 seconds to await discord
        // This is a temporary fix, because the awaitReady method got the bot stuck lately
        Executors.newSingleThreadScheduledExecutor().schedule(
                () -> this.moduleManager.onDiscordReady(this.discord),
                2, TimeUnit.SECONDS
        );
        log.debug("Done starting all modules");
    }

    public JDA getBaseShard() {
        return this.discord.getShardById(0);
    }
}
