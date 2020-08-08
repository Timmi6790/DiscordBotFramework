package de.timmi6790.discord_framework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.core.CoreModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class DiscordBot {
    public static final String BOT_VERSION = "3.0.4";
    @Getter
    private static ModuleManager moduleManager;
    @Getter
    private static SentryClient sentry;
    @Getter
    private static JDA discord;
    @Getter
    private static Path basePath;

    @Getter
    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static void main(final String[] args) throws LoginException, TopicalSortCycleException, InterruptedException, IOException, URISyntaxException {
        setup();
        start();
    }


    public static void setup() throws IOException {
        basePath = Paths.get(".").toAbsolutePath().normalize();

        // Plugins
        final Path pluginsFolderPath = Paths.get(basePath + "/plugins/");
        Files.createDirectories(pluginsFolderPath);

        // Logs
        final Path logsFolderPath = Paths.get(basePath + "/logs/");
        Files.createDirectories(logsFolderPath);

        // Config
        final Path configFolderPath = Paths.get(basePath + "/configs/");
        Files.createDirectories(configFolderPath);
        final Path configPath = Paths.get(configFolderPath + "/config.json");
        if (!Files.exists(configPath)) {
            Files.write(configPath, Collections.singleton(gson.toJson(new Config())));
            DiscordBot.getLogger().info("Created main logging file.");
            System.exit(1);
        }
    }

    public static void start() throws TopicalSortCycleException, LoginException, InterruptedException {
        final Config mainConfig = getConfig();
        if (!mainConfig.getSentry().isEmpty()) {
            sentry = SentryClientFactory.sentryClient(mainConfig.getSentry());
            sentry.setRelease(BOT_VERSION);
        }

        discord = JDABuilder.createLight(mainConfig.getDiscordToken(),
                GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES)
                .setStatus(OnlineStatus.ONLINE)
                .build();

        moduleManager = new ModuleManager();
        moduleManager.registerModules(
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

                new CoreModule()
        );

        discord.awaitReady();
        moduleManager.loadExternalModules();
        moduleManager.startAll();
    }

    public static TaggedLogger getLogger() {
        return Logger.tag("DiscordFramework");
    }

    @SneakyThrows
    public static Config getConfig() {
        final Path mainConfigPath = Paths.get(basePath + "/configs/config.json");
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(mainConfigPath.toString()));
        return gson.fromJson(bufferedReader, Config.class);
    }
}
