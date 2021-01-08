package de.timmi6790.discord_framework.modules.channel.repository;

import de.timmi6790.discord_framework.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.modules.channel.repository.database_mappers.ChannelDbMapper;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

/**
 * Mysql channel repository implementation
 */
public class ChannelRepositoryMysql implements ChannelRepository {
    private static final String GET_CHANNEL = "SELECT channel.id, channel.discordId, disabled, guild.discordId serverDiscordId " +
            "FROM channel " +
            "INNER JOIN guild ON guild.id = channel.guild_id " +
            "WHERE channel.discordId = :discordId " +
            "LIMIT 1;";

    private static final String INSERT_CHANNEL = "INSERT INTO channel(discordId, guild_id) VALUES (:discordId, (SELECT id FROM guild WHERE guild.discordId = :guildId LIMIT 1));";

    private final Jdbi database;

    /**
     * Instantiates a new Channel repository mysql.
     *
     * @param discordShardManager the discord shard manager
     * @param databaseModule      the database module
     * @param guildDbModule       the guildDb module
     */
    public ChannelRepositoryMysql(final ShardManager discordShardManager,
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
        // Make sure that the channel is not present
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_CHANNEL)
                        .bind("discordId", discordChannelId)
                        .bind("guildId", discordGuildID)
                        .execute()
        );

        // Should never throw
        return this.get(discordChannelId).orElseThrow(RuntimeException::new);
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
