package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.command.CommandManager;
import de.timmi6790.discord_framework.modules.core.CoreModule;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionManager;
import de.timmi6790.discord_framework.modules.event_handler.EventManager;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsManager;
import de.timmi6790.discord_framework.modules.rank.RankManager;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import de.timmi6790.external_modules.botlist.BotListModule;
import de.timmi6790.external_modules.mineplexstats.MineplexStatsModule;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jdbi.v3.core.Jdbi;

import javax.security.auth.login.LoginException;
import java.io.File;

public class DiscordBot {
    public static final String BOT_VERSION = "3.0.3";
    @Getter
    private static ModuleManager moduleManager;
    @Getter
    private static SentryClient sentry;
    @Getter
    private static Jdbi database;
    @Getter
    private static JDA discord;
    @Getter
    private static CommandManager commandManager;
    @Getter
    private static EventManager eventManager;
    @Getter
    private static PermissionsManager permissionsManager;
    @Getter
    private static RankManager rankManager;
    @Getter
    private static EmoteReactionManager emoteReactionManager;

    public static void main(final String[] args) throws LoginException, ConfigurationException, TopicalSortCycleException {
        final Configurations configs = new Configurations();
        final Configuration config = configs.properties(new File("config.properties"));

        start(config);
    }

    public static void start(final Configuration config) throws TopicalSortCycleException, LoginException {
        if (!config.getString("sentry.dsn").isEmpty()) {
            sentry = SentryClientFactory.sentryClient(config.getString("sentry.dsn"));
            sentry.setRelease(BOT_VERSION);
        }

        database = Jdbi.create(config.getString("db.url"), config.getString("db.name"), config.getString("db.password"));

        discord = JDABuilder.createLight(config.getString("discord.token"),
                GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching(config.getString("discord.mainCommand") + "help"))
                .build();

        moduleManager = new ModuleManager();
        eventManager = new EventManager();
        permissionsManager = new PermissionsManager();
        rankManager = new RankManager();
        rankManager.loadRanksFromDatabase();

        commandManager = new CommandManager(config.getString("discord.mainCommand"), discord.getSelfUser().getIdLong());
        commandManager.innitDatabase(database);
        eventManager.addEventListener(commandManager);

        emoteReactionManager = new EmoteReactionManager();

        moduleManager.registerModules(
                new StatModule(),
                new AchievementModule(),
                new SettingModule(),

                new BotListModule(),
                new MineplexStatsModule(),
                new CoreModule()
        );
        moduleManager.startAll();
    }

    public static Configuration getConfig() {
        final Configurations configs = new Configurations();
        try {
            final Configuration config = configs.properties(new File("config.properties"));
            config.setProperty("discord.token", null);
            config.setProperty("db.name", null);
            config.setProperty("db.password", null);
            config.setProperty("db.url", null);
            return config;
        } catch (final ConfigurationException ignore) {
        }
        return null;
    }
}
