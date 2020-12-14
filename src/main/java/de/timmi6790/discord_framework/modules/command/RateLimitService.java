package de.timmi6790.discord_framework.modules.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RateLimitService {
    private final Cache<Long, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    private Bucket createBucket(final int minuteLimit, final int hourLimit) {
        return Bucket4j.builder()
                .addLimit(Bandwidth.classic(minuteLimit, Refill.intervally(minuteLimit, Duration.ofMinutes(1))))
                .addLimit(Bandwidth.classic(hourLimit, Refill.intervally(hourLimit, Duration.ofHours(1))))
                .build();
    }

    private Bucket newDefaultBucket() {
        return this.createBucket(30, 300);
    }

    public Bucket resolveBucket(final long userId) {
        // check if the api is valid
        return this.cache.get(userId, key -> this.newDefaultBucket());
    }
}
