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

    private String botId;
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
        this.botId = this.getDiscordBot().getBaseShard().getSelfUser().getId();
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
                    .botId(this.botId)
                    .build();

            this.updateTask = Executors.newScheduledThreadPool(1)
                    .scheduleAtFixedRate(
                            () -> {
                                for (final JDA shard : this.getDiscord().getShards()) {
                                    final JDA.ShardInfo shardInfo = shard.getShardInfo();
                                    this.botListAPI.setStats(
                                            shardInfo.getShardId(),
                                            shardInfo.getShardTotal(),
                                            shard.getGuilds().size()
                                    );
                                }
                            },
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
