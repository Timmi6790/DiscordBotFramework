package de.timmi6790.discord_framework.modules.botlist;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.JDA;
import org.discordbots.api.client.DiscordBotListAPI;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This module is currently only used to sync the guild count with top.gg every 30 minutes
 */
@EqualsAndHashCode(callSuper = true)
public class BotListModule extends AbstractModule {
    private ScheduledFuture<?> updateTask;

    private JDA discord;
    private DiscordBotListAPI botListAPI;

    /**
     * Instantiates a new Bot list module.
     */
    public BotListModule() {
        super("BotList");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.discord = this.getDiscord();
    }

    @Override
    public void onEnable() {
        // Bot list server count update task
        final String discordListToken = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config())
                .getDiscordListToken();
        if (!discordListToken.isEmpty()) {
            this.botListAPI = new DiscordBotListAPI.Builder()
                    .token(discordListToken)
                    .botId(this.discord.getSelfUser().getId())
                    .build();

            this.updateTask = Executors.newScheduledThreadPool(1)
                    .scheduleAtFixedRate(
                            () -> this.botListAPI.setStats(this.getDiscord().getGuilds().size()),
                            0,
                            30,
                            TimeUnit.MINUTES
                    );
        }
    }

    @Override
    public void onDisable() {
        this.updateTask.cancel(true);
    }
}
