package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
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
import de.timmi6790.external_modules.botlist.BotListModule;
import de.timmi6790.external_modules.mineplexstats.MineplexStatsModule;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.security.auth.login.LoginException;
import java.io.File;

public class DiscordBot {
    public static final String BOT_VERSION = "3.0.4";
    @Getter
    private static ModuleManager moduleManager;
    @Getter
    private static SentryClient sentry;
    @Getter
    private static JDA discord;

    public static void main(final String[] args) throws LoginException, ConfigurationException, TopicalSortCycleException, InterruptedException {
        final Configurations configs = new Configurations();
        final Configuration config = configs.properties(new File("config.properties"));

        start(config);
    }

    public static void start(final Configuration config) throws TopicalSortCycleException, LoginException, InterruptedException {
        if (!config.getString("sentry.dsn").isEmpty()) {
            sentry = SentryClientFactory.sentryClient(config.getString("sentry.dsn"));
            sentry.setRelease(BOT_VERSION);
        }

        discord = JDABuilder.createLight(config.getString("discord.token"),
                GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching(config.getString("discord.mainCommand") + "help"))
                .build();

        moduleManager = new ModuleManager();
        moduleManager.registerModules(
                new DatabaseModule(config.getString("db.url"), config.getString("db.name"), config.getString("db.password")),
                new EventModule(),
                new CommandModule(config.getString("discord.mainCommand"), discord.getSelfUser().getIdLong()),

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

        moduleManager.registerModules(
                new BotListModule(),
                new MineplexStatsModule()
        );

        discord.awaitReady();
        moduleManager.startAll();
    }

    @SneakyThrows
    public static Configuration getConfig() {
        return new Configurations().properties(new File("config.properties"));
    }
}
