package de.timmi6790.discord_framework.modules.guild;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.Striped;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.repository.GuildDbRepository;
import de.timmi6790.discord_framework.modules.guild.repository.mysql.GuildDbRepositoryMysql;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@EqualsAndHashCode(callSuper = true)
@Getter
public class GuildDbModule extends AbstractModule {
    private final Striped<Lock> guildCreateLock = Striped.lock(64);
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
                SettingModule.class
        );

        this.addDependencies(
                CommandModule.class
        );

        // Register metrics
        DiscordBot.CACHE_METRICS.addCache("guildDB_guild_cache", this.cache);
    }

    @Override
    public void onInitialize() {
        this.guildDbRepository = new GuildDbRepositoryMysql(
                this.getModuleOrThrow(DatabaseModule.class).getJdbi(),
                this.getDiscord()
        );
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
