package de.timmi6790.discord_framework.modules.channel;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.channel.repository.ChannelRepository;
import de.timmi6790.discord_framework.modules.channel.repository.ChannelRepositoryMysql;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class ChannelDbModule extends AbstractModule {
    @Getter
    private final Cache<Long, ChannelDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private ChannelRepository channelRepository;

    public ChannelDbModule() {
        super("ChannelDb");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                GuildDbModule.class,
                PermissionsModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.channelRepository = new ChannelRepositoryMysql(this);
    }

    protected ChannelDb create(final long discordId, final long guildId) {
        // Make sure that the channel is not present
        return this.get(discordId).orElseGet(() -> this.channelRepository.create(discordId, guildId));
    }

    public Optional<ChannelDb> get(final long discordId) {
        final ChannelDb channelDbCache = this.cache.getIfPresent(discordId);
        if (channelDbCache != null) {
            return Optional.of(channelDbCache);
        }

        final Optional<ChannelDb> channelDbOpt = this.channelRepository.get(discordId);
        channelDbOpt.ifPresent(userDb -> this.cache.put(discordId, userDb));

        return channelDbOpt;
    }

    public ChannelDb getOrCreate(final long discordId, final long guildId) {
        return this.get(discordId).orElseGet(() -> this.create(discordId, guildId));
    }
}
