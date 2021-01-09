package de.timmi6790.discord_framework.modules.guild.repository.mysql;

import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.guild.repository.GuildDbRepository;
import de.timmi6790.discord_framework.modules.guild.repository.mysql.mappers.GuildDbDatabaseMapper;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class GuildDbRepositoryMysql implements GuildDbRepository {
    private static final String GET_GUILD = "SELECT guild.id, discordId, banned " +
            "FROM guild " +
            "WHERE guild.discordId = :discordId " +
            "LIMIT 1;";

    private static final String INSERT_GUILD = "INSERT INTO guild(discordId) VALUES (:discordId);";

    private final Jdbi database;

    public GuildDbRepositoryMysql(final Jdbi database, final ShardManager discord) {
        this.database = database;
        this.database.registerRowMapper(
                GuildDb.class,
                new GuildDbDatabaseMapper(discord)
        );
    }

    @Override
    public GuildDb create(final long discordId) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_GUILD)
                        .bind("discordId", discordId)
                        .execute()
        );

        // Should never throw
        return this.get(discordId).orElseThrow(RuntimeException::new);
    }

    @Override
    public Optional<GuildDb> get(final long discordId) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_GUILD)
                        .bind("discordId", discordId)
                        .mapTo(GuildDb.class)
                        .findFirst()
        );
    }

    @Override
    public void updateSetting(final int guildDatabaseId, final int settingId, final String newValue) {

    }
}
