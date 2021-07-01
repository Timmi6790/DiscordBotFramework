package de.timmi6790.discord_framework.module.modules.guild.repository.mysql;

import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.guild.repository.GuildDbRepository;
import de.timmi6790.discord_framework.module.modules.guild.repository.mysql.mappers.GuildDbMapper;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class GuildDbPostgresRepository implements GuildDbRepository {
    private static final String GET_GUILD = "SELECT guild.discord_id, guild.banned " +
            "FROM guild.guilds guild " +
            "WHERE guild.discord_id = :discordId " +
            "LIMIT 1;";

    private static final String CREATE_GUILD = "INSERT INTO guild.guilds(discord_id) VALUES (:discordId) RETURNING discord_id, banned;";

    private final Jdbi database;

    public GuildDbPostgresRepository(final Jdbi database, final ShardManager discord) {
        this.database = database;
        
        this.database
                .registerRowMapper(new GuildDbMapper(discord));
    }

    @Override
    public GuildDb createGuild(final long discordId) {
        return this.database.withHandle(handle ->
                handle.createQuery(CREATE_GUILD)
                        .bind("discordId", discordId)
                        .mapTo(GuildDb.class)
                        .first()
        );
    }

    @Override
    public Optional<GuildDb> getGuild(final long discordId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_GUILD)
                        .bind("discordId", discordId)
                        .mapTo(GuildDb.class)
                        .findFirst()
        );
    }
}
