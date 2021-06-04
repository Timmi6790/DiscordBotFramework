package de.timmi6790.discord_framework.module.modules.reactions.button.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;

public class CacheExpireAfter implements Expiry<Long, ButtonReaction> {
    @Override
    public long expireAfterCreate(@NonNull final Long key,
                                  @NonNull final ButtonReaction value,
                                  final long currentTime) {
        return TimeUnit.SECONDS.toNanos(value.getDeleteTime());
    }

    @Override
    public long expireAfterUpdate(@NonNull final Long key,
                                  @NonNull final ButtonReaction value,
                                  final long currentTime,
                                  @NonNegative final long currentDuration) {
        return currentDuration;
    }

    @Override
    public long expireAfterRead(@NonNull final Long key,
                                @NonNull final ButtonReaction value,
                                final long currentTime, @NonNegative final
                                long currentDuration) {
        return currentDuration;
    }
}
