package de.timmi6790.discord_framework.module.modules.metric;

import de.timmi6790.discord_framework.module.AbstractModule;
import io.prometheus.client.Gauge;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class MetricModule extends AbstractModule {
    private static final Gauge DISCORD_GUILDS = Gauge.build()
            .name("discord_guilds")
            .help("Discord guilds.")
            .register();

    private static final Gauge DISCORD_SHARDS_RUNNING = Gauge.build()
            .name("discord_shards_running")
            .help("Discord running shards.")
            .register();

    private ScheduledFuture<?> executorService;

    public MetricModule() {
        super("Metric");
    }

    @Override
    public boolean onEnable() {
        this.executorService = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    final ShardManager discord = this.getDiscord();
                    DISCORD_GUILDS.set(discord.getGuilds().size());
                    DISCORD_SHARDS_RUNNING.set(discord.getShardsRunning());
                },
                0, 10, TimeUnit.SECONDS
        );
        return true;
    }

    @Override
    public boolean onDisable() {
        this.executorService.cancel(true);
        return true;
    }
}
