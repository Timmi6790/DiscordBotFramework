package de.timmi6790.discord_framework.module.modules.metric;

import com.google.common.util.concurrent.Striped;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import java.util.*;
import java.util.concurrent.locks.Lock;

public class MultiTaggedTimer {
    private final String name;
    private final String description;
    private final String[] tagNames;
    private final MeterRegistry registry;

    private final Striped<Lock> locks = Striped.lock(16);
    private final Map<String, Timer> timers = new HashMap<>();

    public MultiTaggedTimer(final String name,
                            final String description,
                            final MeterRegistry registry,
                            final String... tags) {
        this.name = name;
        this.description = description;
        this.tagNames = tags.clone();
        this.registry = registry;
    }

    public Timer get(final String... tagValues) {
        final String values = Arrays.toString(tagValues);

        final Lock lock = this.locks.get(values);
        lock.lock();
        try {
            if (tagValues.length != this.tagNames.length) {
                throw new IllegalArgumentException(
                        "Expected args are " + Arrays.toString(this.tagNames) + ", provided tags are " + values
                );
            }

            Timer timer = this.timers.get(values);
            if (timer == null) {
                final List<Tag> tags = new ArrayList<>(this.tagNames.length);
                for (int index = 0; this.tagNames.length > index; index++) {
                    tags.add(
                            new ImmutableTag(
                                    this.tagNames[index],
                                    tagValues[index]
                            )
                    );
                }

                timer = Timer
                        .builder(this.name)
                        .description(this.description)
                        .tags(tags)
                        .register(this.registry);
                this.timers.put(values, timer);
            }
            return timer;
        } finally {
            lock.unlock();
        }
    }
}
