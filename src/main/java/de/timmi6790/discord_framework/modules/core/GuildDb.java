package de.timmi6790.discord_framework.modules.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
@Getter
public class GuildDb {
    private static final String GET_GUILD = "SELECT guild.id, discordId, banned, GROUP_CONCAT(alias.alias) aliases FROM guild " +
            "LEFT JOIN guild_command_alias alias ON alias.guild_id = guild.id " +
            "WHERE guild.discordId = :discordId " +
            "LIMIT 1;";

    private static final String INSERT_GUILD = "INSERT INTO guild(discordId) VALUES (:discordId);";

    @Getter
    private static final LoadingCache<Long, GuildDb> GUILD_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(discordId ->
                    DiscordBot.getDatabase().withHandle(handle -> handle.createQuery(GET_GUILD)
                            .bind("discordId", discordId)
                            .mapTo(GuildDb.class)
                            .findOne()
                            .orElseGet(() -> {
                                handle.createUpdate(INSERT_GUILD)
                                        .bind("discordId", discordId)
                                        .execute();
                                return handle.createQuery(GET_GUILD)
                                        .bind("discordId", discordId)
                                        .mapTo(GuildDb.class)
                                        .first();
                            })
                    )
            );

    private final int databaseId;
    private final long discordId;

    private final boolean banned;

    private final Set<String> commandAliasNames;
    private final Pattern commandAliasPattern;

    private final Map<String, AbstractSetting> properties;

    public GuildDb(final int databaseId, final long discordId, final boolean banned, final Set<String> commandAliasNames, final Map<String, AbstractSetting> properties) {
        this.databaseId = databaseId;
        this.discordId = discordId;
        this.banned = banned;
        this.commandAliasNames = commandAliasNames;
        this.properties = properties;

        // TODO: Escape the alias names or limit the alias names
        if (commandAliasNames.isEmpty()) {
            this.commandAliasPattern = null;

        } else {
            final String aliasPattern = commandAliasNames
                    .stream()
                    .map(alias -> "(?:" + alias + ")")
                    .collect(Collectors.joining("|"));
            this.commandAliasPattern = Pattern.compile("^(?:" + aliasPattern + ")(.*)$)", Pattern.CASE_INSENSITIVE);
        }
    }

    public static GuildDb getOrCreate(final long discordId) {
        return GUILD_CACHE.get(discordId);
    }

    public Optional<Pattern> getCommandAliasPattern() {
        return Optional.ofNullable(this.commandAliasPattern);
    }

    public boolean addCommandAlias(final String alias) {
        // Insert db
        return this.commandAliasNames.add(alias);
    }
}
