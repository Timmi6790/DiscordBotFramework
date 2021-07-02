package de.timmi6790.discord_framework.module.modules.metric;

import de.timmi6790.discord_framework.module.AbstractModule;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.undertow.Undertow;
import io.undertow.util.Headers;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Log4j2
public class MetricModule extends AbstractModule {
    private static final Gauge DISCORD_GUILDS = Gauge.build()
            .name("discord_guilds")
            .help("Discord guilds.")
            .register();

    private static final Gauge DISCORD_SHARDS_RUNNING = Gauge.build()
            .name("discord_shards_running")
            .help("Discord running shards.")
            .register();

    @Getter
    private PrometheusMeterRegistry meterRegistry;

    private Undertow metricsServer;
    private ScheduledFuture<?> executorService;

    public MetricModule() {
        super("Metric");
    }

    private void startMetricsServer() {
        this.metricsServer = Undertow.builder()
                .addHttpListener(8001, "127.0.0.1")
                .setHandler(exchange -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(this.meterRegistry.scrape());
                }).build();
        this.metricsServer.start();
    }

    private void registerJvmMetrics(final MeterRegistry registry) {
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);

        try (final JvmGcMetrics gcMetrics = new JvmGcMetrics()) {
            gcMetrics.bindTo(registry);
        }
    }

    @Override
    public boolean onInitialize() {
        this.meterRegistry = new PrometheusMeterRegistry(
                PrometheusConfig.DEFAULT,
                CollectorRegistry.defaultRegistry,
                Clock.SYSTEM
        );

        this.startMetricsServer();
        this.registerJvmMetrics(this.meterRegistry);

        return true;
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
        this.metricsServer.stop();
        return true;
    }
}
