package de.timmi6790.discord_framework.module.modules.channel;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.Striped;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.channel.repository.ChannelRepository;
import de.timmi6790.discord_framework.module.modules.channel.repository.postgres.ChannelPostgresRepository;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Handles all channel db instances
 */
@EqualsAndHashCode(callSuper = true)
public class ChannelDbModule extends AbstractModule {
    private final Striped<Lock> channelGetLock = Striped.lock(64);
    private final Striped<Lock> channelGetOrCreateLock = Striped.lock(64);

    @Getter
    private final Cache<Long, ChannelDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private ChannelRepository channelRepository;
    private GuildDbModule guildDbModule;

    /**
     * Instantiates a new Channel db module.
     */
    public ChannelDbModule() {
        super("ChannelDb");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                GuildDbModule.class,
                PermissionsModule.class
        );

        this.addLoadAfterDependencies(
                MetricModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.guildDbModule = this.getModuleOrThrow(GuildDbModule.class);
        this.channelRepository = new ChannelPostgresRepository(
                this.getDiscord(),
                this.getModuleOrThrow(DatabaseModule.class),
                this.guildDbModule
        );

        // Register metrics
        this.getModule(MetricModule.class).ifPresent(metric ->
                CaffeineCacheMetrics.monitor(
                        metric.getMeterRegistry(),
                        this.cache,
                        "channelDB_channel"
                )
        );

        return true;
    }

    /**
     * Create a new channel db instance
     *
     * @param discordChannelId the discord channel id
     * @param discordGuildId   the discord guild id
     * @return the channel db instance
     */
    protected ChannelDb create(final long discordChannelId, final long discordGuildId) {
        // Assure that the guild exist
        this.guildDbModule.getOrCreate(discordGuildId);

        final ChannelDb channelDb = this.channelRepository.create(discordChannelId, discordGuildId);
        this.getCache().put(discordChannelId, channelDb);
        return channelDb;
    }

    /**
     * Tries to retrieve the corresponding channel db instance inside the repository
     *
     * @param discordChannelId the discord channel id
     * @return the channel db instance
     */
    public Optional<ChannelDb> get(final long discordChannelId) {
        final Lock lock = this.channelGetLock.get(discordChannelId);
        lock.lock();
        try {
            final ChannelDb channelDbCache = this.cache.getIfPresent(discordChannelId);
            if (channelDbCache != null) {
                return Optional.of(channelDbCache);
            }

            final Optional<ChannelDb> channelDbOpt = this.channelRepository.get(discordChannelId);
            channelDbOpt.ifPresent(userDb -> this.cache.put(discordChannelId, userDb));
            return channelDbOpt;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves or create a discord channel instance
     *
     * @param discordChannelId the discord channel id
     * @param discordGuildId   the discord guild id
     * @return the channel db instance
     */
    public ChannelDb getOrCreate(final long discordChannelId, final long discordGuildId) {
        final Lock lock = this.channelGetOrCreateLock.get(discordChannelId);
        lock.lock();
        try {
            return this.get(discordChannelId)
                    .orElseGet(() -> this.create(discordChannelId, discordGuildId));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves or create a discord channel instance
     *
     * @param discordChannelId the discord channel id
     * @return the channel db instance
     */
    public ChannelDb getOrCreatePrivateMessage(final long discordChannelId) {
        return this.getOrCreate(discordChannelId, GuildDbModule.getPrivateMessageGuildId())
                .setPrivateChannel(true);
    }
}
