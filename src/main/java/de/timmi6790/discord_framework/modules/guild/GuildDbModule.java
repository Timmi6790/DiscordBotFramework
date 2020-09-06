package de.timmi6790.discord_framework.modules.guild;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class GuildDbModule extends AbstractModule {
    private static final String GET_GUILD = "SELECT guild.id, discordId, banned, GROUP_CONCAT(alias.alias) aliases FROM guild " +
            "LEFT JOIN guild_command_alias alias ON alias.guild_id = guild.id " +
            "WHERE guild.discordId = :discordId " +
            "LIMIT 1;";

    private static final String INSERT_GUILD = "INSERT INTO guild(discordId) VALUES (:discordId);";

    @Getter
    private final Cache<Long, GuildDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public GuildDbModule() {
        super("Guild");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                PermissionsModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.getModuleOrThrow(DatabaseModule.class)
                .getJdbi()
                .registerRowMapper(GuildDb.class, new GuildDbMapper());
    }

    private GuildDb create(final long discordId) {
        // Make sure that the guild is not present
        return this.get(discordId).orElseGet(() -> {
            this.getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                    handle.createUpdate(INSERT_GUILD)
                            .bind("discordId", discordId)
                            .execute()
            );

            // Should never throw
            return this.get(discordId).orElseThrow(RuntimeException::new);
        });
    }

    public Optional<GuildDb> get(final long discordId) {
        final GuildDb guildDbCache = this.cache.getIfPresent(discordId);
        if (guildDbCache != null) {
            return Optional.of(guildDbCache);
        }

        final Optional<GuildDb> guildDbOpt = this.getModuleOrThrow(DatabaseModule.class).getJdbi().withHandle(handle ->
                handle.createQuery(GET_GUILD)
                        .bind("discordId", discordId)
                        .mapTo(GuildDb.class)
                        .findFirst()
        );
        guildDbOpt.ifPresent(userDb -> this.cache.put(discordId, userDb));

        return guildDbOpt;
    }

    public GuildDb getOrCreate(final long discordId) {
        return this.get(discordId).orElseGet(() -> this.create(discordId));
    }
}
