package de.timmi6790.discord_framework.modules.guild;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.Striped;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.guild.repository.GuildDbRepository;
import de.timmi6790.discord_framework.modules.guild.repository.mysql.GuildDbRepositoryMysql;
import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@EqualsAndHashCode
@Getter
public class GuildDbModule implements Module {
    private final Striped<Lock> guildCreateLock = Striped.lock(64);
    private final Cache<Long, GuildDb> cache = Caffeine.newBuilder()
            .recordStats()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final GuildDbRepository guildDbRepository;

    public GuildDbModule(final GuildDbRepositoryMysql guildDbRepository) {
        this.guildDbRepository = guildDbRepository;

        // Register metrics
        DiscordBot.CACHE_METRICS.addCache("guildDB_guild_cache", this.cache);
    }

    @Override
    public String getName() {
        return "Guild";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
    }

    protected GuildDb create(final long discordId) {
        // Lock the current discord id to prevent multiple creates
        final Lock lock = this.guildCreateLock.get(discordId);
        lock.lock();
        try {
            // Make sure that the guild is not present
            final Optional<GuildDb> guildDbOpt = this.get(discordId);
            if (guildDbOpt.isPresent()) {
                return guildDbOpt.get();
            }

            final GuildDb guildDb = this.getGuildDbRepository().create(discordId);
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

        final Optional<GuildDb> guildDbOpt = this.getGuildDbRepository().get(discordId);
        guildDbOpt.ifPresent(userDb -> this.getCache().put(discordId, userDb));

        return guildDbOpt;
    }

    public GuildDb getOrCreate(final long discordId) {
        return this.get(discordId).orElseGet(() -> this.create(discordId));
    }
}
