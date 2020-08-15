package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.datatypes.builders.ListBuilder;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.core.CoreModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.feedback.FeedbackModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.FileUtilities;
import de.timmi6790.discord_framework.utilities.ReflectionUtilities;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class DiscordBot {
    public static final String BOT_VERSION = "3.0.5";
    private static DiscordBot instance;

    private final ModuleManager moduleManager;
    private final Path basePath;
    private final List<AbstractModule> internalModules;
    private SentryClient sentry;
    private JDA discord;

    public DiscordBot() {
        this.basePath = Paths.get(".").toAbsolutePath().normalize();

        this.moduleManager = new ModuleManager();
        this.internalModules = new ListBuilder<AbstractModule>(ArrayList::new).addAll(
                new DatabaseModule(),
                new EventModule(),
                new CommandModule(),
                new ConfigModule(),

                new PermissionsModule(),
                new RankModule(),
                new UserDbModule(),
                new GuildDbModule(),
                new ChannelDbModule(),
                new EmoteReactionModule(),

                new StatModule(),
                new AchievementModule(),
                new SettingModule(),
                new FeedbackModule(),

                new CoreModule()
        ).build();
    }

    public static void main(final String[] args) throws LoginException, TopicalSortCycleException, InterruptedException, IOException {
        instance = new DiscordBot();
        instance.start();
    }

    public static DiscordBot getInstance() {
        return instance;
    }

    public static TaggedLogger getLogger() {
        return Logger.tag("DiscordFramework");
    }

    @SneakyThrows
    private Config getConfig() {
        final Path mainConfigPath = Paths.get(this.basePath + "/configs/config.json");
        return FileUtilities.readJsonFile(mainConfigPath, Config.class);
    }

    private void setup() throws IOException {
        // Plugins
        final Path pluginsFolderPath = Paths.get(this.basePath + "/plugins/");
        Files.createDirectories(pluginsFolderPath);

        // Logs
        final Path logsFolderPath = Paths.get(this.basePath + "/logs/");
        Files.createDirectories(logsFolderPath);

        // Config
        final Path configFolderPath = Paths.get(this.basePath + "/configs/");
        Files.createDirectories(configFolderPath);
        final Path configPath = Paths.get(configFolderPath + "/config.json");

        final boolean firstInnit;
        final Config config;
        if (!Files.exists(configPath)) {
            firstInnit = true;
            config = new Config();
        } else {
            firstInnit = false;
            config = this.getConfig();
        }

        final Config newConfig = ReflectionUtilities.deepCopy(config);
        for (final AbstractModule module : this.internalModules) {
            newConfig.getEnabledModules().putIfAbsent(module.getName(), true);
        }

        FileUtilities.saveToJsonIfChanged(configPath, config, newConfig);
        if (firstInnit) {
            DiscordBot.getLogger().info("Created main logging file.");
            System.exit(1);
        }
    }

    public void start() throws TopicalSortCycleException, LoginException, InterruptedException, IOException {
        this.setup();

        final Config mainConfig = this.getConfig();
        if (!mainConfig.getSentry().isEmpty()) {
            this.sentry = SentryClientFactory.sentryClient(mainConfig.getSentry());
            this.sentry.setRelease(BOT_VERSION);
        }

        // Modules
        this.internalModules.stream()
                .filter(module -> mainConfig.getEnabledModules().containsKey(module.getName()))
                .filter(module -> mainConfig.getEnabledModules().get(module.getName()))
                .forEach(this.moduleManager::registerModule);
        this.moduleManager.loadExternalModules();

        // Discord
        final Set<GatewayIntent> requiredGatewayIntents = new HashSet<>();
        for (final AbstractModule loadedModule : this.moduleManager.getLoadedModules().values()) {
            requiredGatewayIntents.addAll(loadedModule.getRequiredGatewayIntents());
        }

        getLogger().debug("Starting discord with {} gateway intents.", requiredGatewayIntents);
        this.discord = JDABuilder.createLight(mainConfig.getDiscordToken(), requiredGatewayIntents)
                .setStatus(OnlineStatus.ONLINE)
                .build();

        this.moduleManager.initializeAll();
        this.discord.awaitReady();
        this.moduleManager.startAll();
    }
}
