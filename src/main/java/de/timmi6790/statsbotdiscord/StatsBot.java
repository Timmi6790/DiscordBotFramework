package de.timmi6790.statsbotdiscord;

import de.timmi6790.statsbotdiscord.modules.achievement.AchievementManager;
import de.timmi6790.statsbotdiscord.modules.command.CommandManager;
import de.timmi6790.statsbotdiscord.modules.core.CoreModule;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionManager;
import de.timmi6790.statsbotdiscord.modules.eventhandler.EventManager;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
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
import org.jdbi.v3.core.Jdbi;

import javax.security.auth.login.LoginException;
import java.io.File;

public class StatsBot {
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

    public static void main(final String[] args) throws LoginException, ConfigurationException {
        final Configuration config;
        final Configurations configs = new Configurations();
        config = configs.properties(new File("config.properties"));

        sentry = SentryClientFactory.sentryClient(config.getString("sentry.dsn"));
        sentry.setRelease("3.0.0");

        database = Jdbi.create(config.getString("db.url"), config.getString("db.name"), config.getString("db.password"));

        discord = new JDABuilder(AccountType.BOT)
                .setGuildSubscriptionsEnabled(false)
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
    }
}
