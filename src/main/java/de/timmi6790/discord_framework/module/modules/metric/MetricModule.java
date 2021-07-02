package de.timmi6790.discord_framework.module.modules.metric;

import de.timmi6790.discord_framework.module.AbstractModule;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.undertow.Undertow;
import io.undertow.util.Headers;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.sharding.ShardManager;

@EqualsAndHashCode(callSuper = true)
@Log4j2
public class MetricModule extends AbstractModule {
    @Getter
    private PrometheusMeterRegistry meterRegistry;

    private Undertow metricsServer;

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

    private void registerMetrics(final MeterRegistry registry) {
        // Jvm metrics
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);

        try (final JvmGcMetrics gcMetrics = new JvmGcMetrics()) {
            gcMetrics.bindTo(registry);
        }

        // Discord metrics
        final ShardManager discord = this.getDiscord();
        Gauge
                .builder("discord.guilds", discord, d -> d.getGuilds().size())
                .description("discord guild count")
                .register(registry);

        Gauge
                .builder("discord.shards", discord, ShardManager::getShardsRunning)
                .tags("state", "running")
                .description("discord shards")
                .register(registry);
        Gauge
                .builder("discord.shards", discord, ShardManager::getShardsQueued)
                .tags("state", "queued")
                .description("discord shards")
                .register(registry);

        Gauge
                .builder("discord.gateway.ping", discord, ShardManager::getAverageGatewayPing)
                .description("discord average gateway ping")
                .register(registry);
    }

    @Override
    public boolean onInitialize() {
        this.meterRegistry = new PrometheusMeterRegistry(
                PrometheusConfig.DEFAULT,
                CollectorRegistry.defaultRegistry,
                Clock.SYSTEM
        );

        this.startMetricsServer();
        this.registerMetrics(this.meterRegistry);

        return true;
    }

    @Override
    public boolean onDisable() {
        this.metricsServer.stop();
        return true;
    }
}
