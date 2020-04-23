package de.timmi6790.statsbotdiscord.modules.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.setting.Setting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@ToString
@EqualsAndHashCode
@Getter
public class GuildDb {
    @Getter
    private final static Cache<Long, GuildDb> GUILD_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final int databaseId;
    private final long discordId;

    private final boolean banned;

    private final Set<String> commandAliasNames;
    private final Pattern commandAliasPattern;

    private final Map<String, Setting> properties;

    public GuildDb(final int databaseId, final long discordId, final boolean banned, final Set<String> commandAliasNames, final Map<String, Setting> properties) {
        this.databaseId = databaseId;
        this.discordId = discordId;
        this.banned = banned;
        this.commandAliasNames = commandAliasNames;
        this.properties = properties;

        // TODO: Escape the alias names or limit the alias names
        final StringBuilder pattern = new StringBuilder();
        pattern.append("^(");
        for (final String alias : commandAliasNames) {
            pattern.append("(").append(alias).append(")?");
        }
        pattern.append(")");

        this.commandAliasPattern = Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE);
    }

    public static Optional<GuildDb> get(final long discordId) {
        final GuildDb guildDb = GUILD_CACHE.getIfPresent(discordId);
        if (guildDb != null) {
            return Optional.of(guildDb);
        }

        final Optional<GuildDb> guildOpt = StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT guild.id, discordId, banned, GROUP_CONCAT(alias.alias) aliases FROM guild " +
                        "LEFT JOIN guild_command_alias alias ON alias.guild_id = guild.id " +
                        "WHERE guild.discordId = :discordId " +
                        "LIMIT 1;")
                        .bind("discordId", discordId)
                        .mapTo(GuildDb.class)
                        .findOne()
        );

        guildOpt.ifPresent(g -> GUILD_CACHE.put(discordId, g));
        return guildOpt;
    }

    public static GuildDb getOrCreate(final long discordId) {
        return GuildDb.get(discordId).orElseGet(() -> {
            StatsBot.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO guild(discordId) VALUES (:discordId);")
                            .bind("discordId", discordId)
                            .execute()
            );

            return GuildDb.get(discordId).orElseThrow(RuntimeException::new);
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
