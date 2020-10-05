package de.timmi6790.discord_framework.modules.channel;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class ChannelDbModule extends AbstractModule {
    private static final String GET_CHANNEL = "SELECT channel.id, channel.discordId, disabled, guild.discordId serverDiscordId " +
            "FROM channel " +
            "INNER JOIN guild ON guild.id = channel.guild_id " +
            "WHERE channel.discordId = :discordId " +
            "LIMIT 1;";

    private static final String INSERT_CHANNEL = "INSERT INTO channel(discordId, guild_id) VALUES (:discordId, (SELECT id FROM guild WHERE guild.discordId = :guildId LIMIT 1));";
    @Getter
    private final Cache<Long, ChannelDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private Jdbi database;

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
        this.database = this.getModuleOrThrow(DatabaseModule.class).getJdbi();
        this.database.registerRowMapper(ChannelDb.class, new ChannelDbMapper(this.getModuleOrThrow(GuildDbModule.class)));
    }

    protected ChannelDb create(final long discordId, final long guildId) {
        // Make sure that the channel is not present
        return this.get(discordId).orElseGet(() -> {
            this.database.useHandle(handle ->
                    handle.createUpdate(INSERT_CHANNEL)
                            .bind("discordId", discordId)
                            .bind("guildId", guildId)
                            .execute()
            );

            // Should never throw
            return this.get(discordId).orElseThrow(RuntimeException::new);
        });
    }

    public Optional<ChannelDb> get(final long discordId) {
        final ChannelDb channelDbCache = this.cache.getIfPresent(discordId);
        if (channelDbCache != null) {
            return Optional.of(channelDbCache);
        }

        final Optional<ChannelDb> channelDbOpt = this.database.withHandle(handle ->
                handle.createQuery(GET_CHANNEL)
                        .bind("discordId", discordId)
                        .mapTo(ChannelDb.class)
                        .findFirst()
        );
        channelDbOpt.ifPresent(userDb -> this.cache.put(discordId, userDb));

        return channelDbOpt;
    }

    public ChannelDb getOrCreate(final long discordId, final long guildId) {
        return this.get(discordId).orElseGet(() -> this.create(discordId, guildId));
    }
}
