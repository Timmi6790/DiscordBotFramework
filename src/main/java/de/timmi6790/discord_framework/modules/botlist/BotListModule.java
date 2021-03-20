package de.timmi6790.discord_framework.modules.botlist;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.discordbots.api.client.DiscordBotListAPI;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This module is currently only used to sync the guild count with top.gg every 30 minutes
 */
@EqualsAndHashCode
public class BotListModule implements Module {
    private final DiscordBot discordBot;
    private final ConfigModule configModule;

    private ScheduledFuture<?> updateTask;

    private String botId;
    private DiscordBotListAPI botListAPI;

    /**
     * Instantiates a new Bot list module.
     */
    public BotListModule(final DiscordBot discordBot, final ConfigModule configModule) {
        this.discordBot = discordBot;
        this.configModule = configModule;
    }

    @Override
    public String getName() {
        return "BotList";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
    }

    @Override
    public void onDiscordReady(final ShardManager shardManager) {
        // Bot list server count update task
        this.botId = this.discordBot.getBaseShard().getSelfUser().getId();
        final String discordListToken = this.configModule
                .registerAndGetConfig(this, new Config())
                .getDiscordListToken();
        if (!discordListToken.isEmpty()) {
            this.botListAPI = new DiscordBotListAPI.Builder()
                    .token(discordListToken)
                    .botId(this.botId)
                    .build();

            this.updateTask = Executors.newScheduledThreadPool(1)
                    .scheduleAtFixedRate(() -> {
                                for (final JDA shard : shardManager.getShards()) {
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
