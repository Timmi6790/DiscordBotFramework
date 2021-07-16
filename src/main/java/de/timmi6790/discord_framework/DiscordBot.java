package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.ModuleStatus;
import de.timmi6790.discord_framework.module.provider.providers.InternalModuleProvider;
import de.timmi6790.discord_framework.module.provider.providers.jar.JarModuleProvider;
import de.timmi6790.discord_framework.utilities.commons.GsonUtilities;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
@Log4j2
public class DiscordBot {
    public static final String BOT_VERSION = "3.2.1";

    private static final DiscordBot INSTANCE = new DiscordBot();

    private final ModuleManager moduleManager = new ModuleManager();
    private ShardManager discord;

    public static void main(final String[] args) throws LoginException, TopicalSortCycleException, IOException {
        DiscordBot.getInstance().start();
    }

    public static DiscordBot getInstance() {
        return INSTANCE;
    }

    @SneakyThrows
    protected Config getConfig() {
        final Path mainConfigPath = Paths.get("./configs/config.json");
        return GsonUtilities.readJsonFile(mainConfigPath, Config.class).orElseThrow(RuntimeException::new);
    }

    protected boolean setup() throws IOException {
        // Config
        final Path configFolderPath = Paths.get("./configs/");
        Files.createDirectories(configFolderPath);
        final Path configPath = Paths.get(configFolderPath + "/config.json");

        final boolean firstInnit = !Files.exists(configPath);
        final Config config = firstInnit ? new Config() : this.getConfig();
        GsonUtilities.saveToJson(configPath, config);
        if (firstInnit) {
            log.info("Created main config file.");
            return false;
        }

        return true;
    }

    public void start() throws TopicalSortCycleException, LoginException, IOException {
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
        this.moduleManager.addModuleProviders(
                new InternalModuleProvider(),
                new JarModuleProvider()
        );
        this.moduleManager.loadModules();

        // Discord
        final Set<GatewayIntent> requiredGatewayIntents = new HashSet<>();
        for (final AbstractModule loadedModule : this.moduleManager.getModules(ModuleStatus.REGISTERED)) {
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
