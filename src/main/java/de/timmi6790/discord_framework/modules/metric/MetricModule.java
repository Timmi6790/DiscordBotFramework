package de.timmi6790.discord_framework.modules.metric;


import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import io.prometheus.client.Gauge;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode
public class MetricModule implements Module {
    private static final Gauge DISCORD_GUILDS = Gauge.build()
            .name("discord_guilds")
            .help("Discord guilds.")
            .register();

    private static final Gauge DISCORD_SHARDS_RUNNING = Gauge.build()
            .name("discord_shards_running")
            .help("Discord running shards.")
            .register();

    private ScheduledFuture<?> executorService;
    
    @Override
    public String getName() {
        return "Metric";
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
    public void onDiscordReady(ShardManager shardManager) {
        this.executorService = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    DISCORD_GUILDS.set(shardManager.getGuilds().size());
                    DISCORD_SHARDS_RUNNING.set(shardManager.getShardsRunning());
                },
                0, 10, TimeUnit.SECONDS
        );
    }

    @Override
    public void onDisable() {
        this.executorService.cancel(true);
    }
}
