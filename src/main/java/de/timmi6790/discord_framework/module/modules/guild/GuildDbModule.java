package de.timmi6790.discord_framework.module.modules.guild;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.Striped;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.guild.repository.GuildDbRepository;
import de.timmi6790.discord_framework.module.modules.guild.repository.postgres.GuildDbPostgresRepository;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@EqualsAndHashCode(callSuper = true)
@Getter
public class GuildDbModule extends AbstractModule {
    private final Striped<Lock> guildCreateLock = Striped.lock(64);
    private final Striped<Lock> guildGetOrCreateLock = Striped.lock(64);
    private final Cache<Long, GuildDb> cache = Caffeine.newBuilder()
            .recordStats()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private GuildDbRepository guildDbRepository;

    public GuildDbModule() {
        super("Guild");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                PermissionsModule.class
        );

        this.addLoadAfterDependencies(
                SettingModule.class,
                MetricModule.class
        );

        this.addDependencies(
                CommandModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.guildDbRepository = new GuildDbPostgresRepository(
                this.getModuleOrThrow(DatabaseModule.class).getJdbi(),
                this.getDiscord()
        );

        // Register metrics
        this.getModule(MetricModule.class).ifPresent(metric ->
                CaffeineCacheMetrics.monitor(
                        metric.getMeterRegistry(),
                        this.cache,
                        "guildDB_guild"
                )
        );

        return true;
    }

    protected GuildDb create(final long discordId) {
        // Lock the current discord id to prevent multiple creates
        final Lock lock = this.guildCreateLock.get(discordId);
        lock.lock();
        try {
            final GuildDb guildDb = this.getGuildDbRepository().createGuild(discordId);
            this.getCache().put(discordId, guildDb);
            return guildDb;
        } finally {
            lock.unlock();
        }
    }

    public Optional<GuildDb> get(final long discordId) {
        final GuildDb guildDbCache = this.getCache().getIfPresent(discordId);
        if (guildDbCache != null) {
            return Optional.of(guildDbCache);
        }

        final Lock lock = this.guildCreateLock.get(discordId);
        lock.lock();
        try {
            final Optional<GuildDb> guildDbOpt = this.getGuildDbRepository().getGuild(discordId);
            guildDbOpt.ifPresent(userDb -> this.getCache().put(discordId, userDb));

            return guildDbOpt;
        } finally {
            lock.unlock();
        }
    }

    public GuildDb getOrCreate(final long discordId) {
        final Lock lock = this.guildGetOrCreateLock.get(discordId);
        lock.lock();
        try {
            return this.get(discordId).orElseGet(() -> this.create(discordId));
        } finally {
            lock.unlock();
        }
    }
}
