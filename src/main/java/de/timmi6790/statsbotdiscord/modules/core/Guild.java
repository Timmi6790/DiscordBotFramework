package de.timmi6790.statsbotdiscord.modules.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class Guild {
    private final static Cache<Long, Guild> GUILD_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final int databaseId;
    private final long discordId;

    private final boolean banned;

    private final Set<String> commandAliasNames;
    private final Map<String, Setting> properties;

    public static Optional<Guild> get(final long discordId) {
        final Guild guild = GUILD_CACHE.getIfPresent(discordId);
        if (guild != null) {
            return Optional.of(guild);
        }

        final Optional<Guild> guildOpt = StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT guild.id, discordId, banned, GROUP_CONCAT(alias.alias) aliases FROM guild " +
                        "LEFT JOIN guild_command_alias alias ON alias.guild_id = guild.id " +
                        "WHERE guild.discordId = :discordId " +
                        "LIMIT 1;")
                        .bind("discordId", discordId)
                        .mapTo(Guild.class)
                        .findOne()
        );

        guildOpt.ifPresent(g -> GUILD_CACHE.put(discordId, g));
        return guildOpt;
    }

    public static Guild getOrCreate(final long discordId) {
        return Guild.get(discordId).orElseGet(() -> {
            StatsBot.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO guild(discordId) VALUES (:discordId);")
                            .bind("discordId", discordId)
                            .execute()
            );

            return Guild.get(discordId).orElseThrow(RuntimeException::new);
        });
    }

    public boolean addCommandAlias(final String alias) {
        if (this.commandAliasNames.add(alias)) {
            // Insert db

            return true;
        }

        return false;
    }
}
