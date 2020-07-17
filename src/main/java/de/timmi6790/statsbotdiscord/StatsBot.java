package de.timmi6790.statsbotdiscord;

import de.timmi6790.statsbotdiscord.modules.ModuleManager;
import de.timmi6790.statsbotdiscord.modules.achievement.AchievementManager;
import de.timmi6790.statsbotdiscord.modules.botlist.BotListModule;
import de.timmi6790.statsbotdiscord.modules.command.CommandManager;
import de.timmi6790.statsbotdiscord.modules.core.CoreModule;
import de.timmi6790.statsbotdiscord.modules.emotereaction.EmoteReactionManager;
import de.timmi6790.statsbotdiscord.modules.eventhandler.EventManager;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.permisssion.PermissionsManager;
import de.timmi6790.statsbotdiscord.modules.rank.RankManager;
import de.timmi6790.statsbotdiscord.modules.setting.SettingManager;
import de.timmi6790.statsbotdiscord.modules.stat.StatManager;
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

public class StatsBot {
    public static final String BOT_VERSION = "3.0.2";
    @Getter
    private static final ModuleManager moduleManager = new ModuleManager();
    @Getter
    private static final SettingManager settingManager = new SettingManager();
    @Getter
    private static SentryClient sentry;
    @Getter
    private static Jdbi database;
    @Getter
    private static JDA discord;
    @Getter
    private static CommandManager commandManager;
    @Getter
    private static StatManager statManager;
    @Getter
    private static EventManager eventManager;
    @Getter
    private static PermissionsManager permissionsManager;
    @Getter
    private static RankManager rankManager;
    @Getter
    private static AchievementManager achievementManager;
    @Getter
    private static EmoteReactionManager emoteReactionManager;

    public static void main(final String[] args) throws LoginException, ConfigurationException {
        final Configurations configs = new Configurations();
        final Configuration config = configs.properties(new File("config.properties"));

        if (!config.getString("sentry.dsn").isEmpty()) {
            sentry = SentryClientFactory.sentryClient(config.getString("sentry.dsn"));
            sentry.setRelease(BOT_VERSION);
        }

        database = Jdbi.create(config.getString("db.url"), config.getString("db.name"), config.getString("db.password"));

        discord = JDABuilder.createLight(config.getString("discord.token"),
                GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching(config.getString("discord.mainCommand") + " help"))
                .build();

        eventManager = new EventManager();
        permissionsManager = new PermissionsManager();
        rankManager = new RankManager();
        rankManager.loadRanksFromDatabase();

        commandManager = new CommandManager(config.getString("discord.mainCommand"), discord.getSelfUser().getIdLong());
        commandManager.innitDatabase(database);
        eventManager.addEventListener(commandManager);

        emoteReactionManager = new EmoteReactionManager();
        statManager = new StatManager();
        achievementManager = new AchievementManager();

        moduleManager.registerModules(
                new MineplexStatsModule(),
                new CoreModule(),
                new BotListModule()
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
