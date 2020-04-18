package de.timmi6790.statsbotdiscord.modules.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
public class Channel {
    private final static Cache<Long, Channel> CHANNEL_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final int databaseId;
    private final long discordId;

    private final Guild guild;

    private boolean disabled;

    public static Optional<Channel> get(final long discordId) {
        final Channel channel = CHANNEL_CACHE.getIfPresent(discordId);
        if (channel != null) {
            return Optional.of(channel);
        }

        final Optional<Channel> channelOpt = StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT channel.id, channel.discordId, disabled, guild.discordId serverDiscordId FROM channel " +
                        "INNER JOIN guild ON guild.id = channel.guild_id " +
                        "WHERE channel.discordId = :discordId " +
                        "LIMIT 1;")
                        .bind("discordId", discordId)
                        .mapTo(Channel.class)
                        .findOne()
        );

        channelOpt.ifPresent(c -> CHANNEL_CACHE.put(discordId, c));
        return channelOpt;
    }

    public static Channel getOrCreate(final long discordId, final long serverDiscordId) {
        return Channel.get(discordId).orElseGet(() -> {
            StatsBot.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO channel(discordId, guild_id) VALUES (:discordId, (SELECT id FROM guild WHERE guild.discordId = :serverDiscordId LIMIT 1));")
                            .bind("discordId", discordId)
                            .bind("serverDiscordId", serverDiscordId)
                            .execute()
            );

            return Channel.get(discordId).orElseThrow(RuntimeException::new);
        });
    }
}
