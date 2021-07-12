package de.timmi6790.discord_framework.module.modules.command.listeners;

import de.timmi6790.discord_framework.module.modules.command.events.PostCommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.metric.MultiTaggedTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

public class MetricListener {
    private final MultiTaggedTimer multiTimer;

    public MetricListener(final MetricModule metricModule) {
        final MeterRegistry registry = metricModule.getMeterRegistry();
        this.multiTimer = new MultiTaggedTimer(
                "command.execution_time",
                "Command execution time",
                registry,
                "command",
                "cause",
                "result"
        );
    }

    @SubscribeEvent
    public void onCommandExecutionPost(final PostCommandExecutionEvent event) {
        final Timer timer = this.multiTimer.get(
                event.getCommand().getName(),
                event.getParameters().getCommandCause().getReason(),
                event.getCommandResult().getExitReason()
        );
        timer.record(event.getExecutionTimeInNano(), TimeUnit.NANOSECONDS);
    }
}
