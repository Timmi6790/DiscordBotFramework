package de.timmi6790.discord_framework.modules.channel;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.Striped;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.channel.repository.ChannelRepository;
import de.timmi6790.discord_framework.modules.channel.repository.mysql.ChannelRepositoryMysql;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
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
    private final Striped<Lock> channelCreateLock = Striped.lock(64);

    @Getter
    private final Cache<Long, ChannelDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private ChannelRepository channelRepository;

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

        // Register metrics
        DiscordBot.CACHE_METRICS.addCache("channelDB_channel_cache", this.cache);
    }

    @Override
    public void onInitialize() {
        this.channelRepository = new ChannelRepositoryMysql(
                this.getDiscord(),
                this.getModuleOrThrow(DatabaseModule.class),
                this.getModuleOrThrow(GuildDbModule.class)
        );
    }

    /**
     * Create a new channel db instance
     *
     * @param discordChannelId the discord channel id
     * @param discordGuildId   the discord guild id
     * @return the channel db instance
     */
    protected ChannelDb create(final long discordChannelId, final long discordGuildId) {
        // Lock the current discord id to prevent multiple creates
        final Lock lock = this.channelCreateLock.get(discordChannelId);
        lock.lock();
        try {
            // Make sure that the channel is not present
            final Optional<ChannelDb> channelDbOpt = this.get(discordChannelId);
            if (channelDbOpt.isPresent()) {
                return channelDbOpt.get();
            }

            final ChannelDb channelDb = this.channelRepository.create(discordChannelId, discordGuildId);
            this.getCache().put(discordChannelId, channelDb);
            return channelDb;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tries to retrieve the corresponding channel db instance inside the repository
     *
     * @param discordChannelId the discord channel id
     * @return the channel db instance
     */
    public Optional<ChannelDb> get(final long discordChannelId) {
        final ChannelDb channelDbCache = this.cache.getIfPresent(discordChannelId);
        if (channelDbCache != null) {
            return Optional.of(channelDbCache);
        }

        final Optional<ChannelDb> channelDbOpt = this.channelRepository.get(discordChannelId);
        channelDbOpt.ifPresent(userDb -> this.cache.put(discordChannelId, userDb));

        return channelDbOpt;
    }

    /**
     * Retrieves or create a discord channel instance
     *
     * @param discordChannelId the discord channel id
     * @param discordGuildId   the discord guild id
     * @return the channel db instance
     */
    public ChannelDb getOrCreate(final long discordChannelId, final long discordGuildId) {
        return this.get(discordChannelId).orElseGet(() -> this.create(discordChannelId, discordGuildId));
    }
}
