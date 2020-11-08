package de.timmi6790.discord_framework.modules.channel.repository;

import de.timmi6790.discord_framework.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class ChannelRepositoryMysql implements ChannelRepository {
    private static final String GET_CHANNEL = "SELECT channel.id, channel.discordId, disabled, guild.discordId serverDiscordId " +
            "FROM channel " +
            "INNER JOIN guild ON guild.id = channel.guild_id " +
            "WHERE channel.discordId = :discordId " +
            "LIMIT 1;";

    private static final String INSERT_CHANNEL = "INSERT INTO channel(discordId, guild_id) VALUES (:discordId, (SELECT id FROM guild WHERE guild.discordId = :guildId LIMIT 1));";

    private final Jdbi database;

    public ChannelRepositoryMysql(final ChannelDbModule module) {
        this.database = module.getModuleOrThrow(DatabaseModule.class).getJdbi();
        this.database.registerRowMapper(
                ChannelDb.class,
                new ChannelDbMapper(
                        module.getModuleOrThrow(GuildDbModule.class),
                        module.getDiscord()
                )
        );
    }

    @Override
    public ChannelDb create(final long discordId, final long guildId) {
        // Make sure that the channel is not present
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_CHANNEL)
                        .bind("discordId", discordId)
                        .bind("guildId", guildId)
                        .execute()
        );

        // Should never throw
        return this.get(discordId).orElseThrow(RuntimeException::new);
    }

    @Override
    public Optional<ChannelDb> get(final long discordId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_CHANNEL)
                        .bind("discordId", discordId)
                        .mapTo(ChannelDb.class)
                        .findFirst()
        );
    }
}
