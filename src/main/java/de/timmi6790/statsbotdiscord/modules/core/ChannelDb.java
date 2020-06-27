package de.timmi6790.statsbotdiscord.modules.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
public class ChannelDb {
    private static final String GET_CHANNEL = "SELECT channel.id, channel.discordId, disabled, guild.discordId serverDiscordId FROM channel " +
            "INNER JOIN guild ON guild.id = channel.guild_id " +
            "WHERE channel.discordId = :discordId " +
            "LIMIT 1;";
    private static final String INSERT_CHANNEL = "INSERT INTO channel(discordId, guild_id) VALUES (:discordId, (SELECT id FROM guild WHERE guild.discordId = :serverDiscordId LIMIT 1));";

    @Getter
    private static final Cache<Long, ChannelDb> CHANNEL_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final int databaseId;
    private final long discordId;

    private final GuildDb guildDb;

    private boolean disabled;

    public static ChannelDb getOrCreate(final long discordId, final long serverDiscordId) {
        ChannelDb channel = CHANNEL_CACHE.getIfPresent(discordId);
        if (channel != null) {
            return channel;
        }

        channel = StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery(GET_CHANNEL)
                        .bind("discordId", discordId)
                        .mapTo(ChannelDb.class)
                        .findOne()
                        .orElseGet(() -> {
                            handle.createUpdate(INSERT_CHANNEL)
                                    .bind("discordId", discordId)
                                    .bind("serverDiscordId", serverDiscordId)
                                    .execute();
                            return handle.createQuery(GET_CHANNEL)
                                    .bind("discordId", discordId)
                                    .mapTo(ChannelDb.class).first();
                        })
        );

        CHANNEL_CACHE.put(discordId, channel);
        return channel;
    }
}
