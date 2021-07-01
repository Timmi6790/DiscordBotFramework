package de.timmi6790.discord_framework.module.modules.channel.repository.postgres;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.channel.repository.ChannelRepository;
import de.timmi6790.discord_framework.module.modules.channel.repository.postgres.mappers.ChannelDbMapper;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

/**
 * Postgres channel repository implementation
 */
public class ChannelPostgresRepository implements ChannelRepository {
    private static final String GET_CHANNEL = "SELECT channel.discord_id, channel.disabled, channel.guild_id +" +
            "FROM channel.channels channel " +
            "WHERE channel.discord_id = :discordId " +
            "LIMIT 1;";

    private static final String INSERT_CHANNEL = "INSERT INTO channel.channels(discord_id, guild_id) VALUES (:discordId, :guildId) RETURNING discord_id, disabled, guild_id;";

    private final Jdbi database;

    /**
     * Instantiates a new Channel repository.
     *
     * @param discordShardManager the discord shard manager
     * @param databaseModule      the database module
     * @param guildDbModule       the guildDb module
     */
    public ChannelPostgresRepository(final ShardManager discordShardManager,
                                     final DatabaseModule databaseModule,
                                     final GuildDbModule guildDbModule) {
        this.database = databaseModule.getJdbi();
        this.database.registerRowMapper(
                new ChannelDbMapper(
                        guildDbModule,
                        discordShardManager
                )
        );
    }

    @Override
    public ChannelDb create(final long discordChannelId, final long discordGuildID) {
        return this.database.withHandle(handle ->
                handle.createQuery(INSERT_CHANNEL)
                        .bind("discordId", discordChannelId)
                        .bind("guildId", discordGuildID)
                        .mapTo(ChannelDb.class)
                        .first()
        );
    }

    @Override
    public Optional<ChannelDb> get(final long discordChannelId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_CHANNEL)
                        .bind("discordId", discordChannelId)
                        .mapTo(ChannelDb.class)
                        .findFirst()
        );
    }
}
