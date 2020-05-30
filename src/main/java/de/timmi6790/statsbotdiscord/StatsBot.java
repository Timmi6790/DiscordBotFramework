package de.timmi6790.statsbotdiscord;

import de.timmi6790.statsbotdiscord.modules.ModuleManager;
import de.timmi6790.statsbotdiscord.modules.achievement.AchievementManager;
import de.timmi6790.statsbotdiscord.modules.command.CommandManager;
import de.timmi6790.statsbotdiscord.modules.core.CoreModule;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionManager;
import de.timmi6790.statsbotdiscord.modules.eventhandler.EventManager;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.setting.SettingManager;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import lombok.Getter;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.discordbots.api.client.DiscordBotListAPI;
import org.jdbi.v3.core.Jdbi;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StatsBot {
    public final static String BOT_VERSION = "3.0.0";

    @Getter
    private static SentryClient sentry;
    @Getter
    private static Jdbi database;

    @Getter
    private static JDA discord;
    @Getter
    private static final ModuleManager moduleManager = new ModuleManager();
    @Getter
    private static CommandManager commandManager;
    @Getter
    private static EventManager eventManager;
    @Getter
    private static final AchievementManager achievementManager = new AchievementManager();
    @Getter
    private static EmoteReactionManager emoteReactionManager;
    @Getter
    private static final SettingManager settingManager = new SettingManager();

    public static void main(final String[] args) throws LoginException, ConfigurationException {
        final Configurations configs = new Configurations();
        final Configuration config = configs.properties(new File("config.properties"));

        if (!config.getString("sentry.dsn").isEmpty()) {
            sentry = SentryClientFactory.sentryClient(config.getString("sentry.dsn"));
            sentry.setRelease(BOT_VERSION);
        }

        database = Jdbi.create(config.getString("db.url"), config.getString("db.name"), config.getString("db.password"));

        discord = new JDABuilder(AccountType.BOT)
                .setToken(config.getString("discord.token"))

                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching(config.getString("discord.mainCommand") + " help"))

                .build();

        eventManager = new EventManager();
        commandManager = new CommandManager(config.getString("discord.mainCommand"));
        emoteReactionManager = new EmoteReactionManager();

        moduleManager.registerModules(
                new MineplexStatsModule(),
                new CoreModule()
        );
        moduleManager.startAll();

        // Bot list server count update task
        if (!config.getString("discord.discordListToken").isEmpty()) {
            final DiscordBotListAPI botListAPI = new DiscordBotListAPI.Builder()
                    .token(config.getString("discord.discordListToken"))
                    .botId(discord.getSelfUser().getId())
                    .build();

            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> botListAPI.setStats(discord.getGuilds().size()), 0, 30, TimeUnit.MINUTES);
        }
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
