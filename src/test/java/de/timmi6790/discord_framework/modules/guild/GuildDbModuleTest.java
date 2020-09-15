package de.timmi6790.discord_framework.modules.guild;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Testcontainers
class GuildDbModuleTest {
    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = (MariaDBContainer) new MariaDBContainer().withClasspathResourceMapping(
            "tables.sql",
            "/docker-entrypoint-initdb.d/createTables.sql",
            BindMode.READ_ONLY
    );
    private static final AtomicLong DISCORD_IDS = new AtomicLong(0);
    private static GuildDbModule guildDbModule;

    @BeforeAll
    static void setUp() {
        guildDbModule = new GuildDbModule();

        guildDbModule.database = Jdbi.create(MARIA_DB_CONTAINER.getJdbcUrl(), MARIA_DB_CONTAINER.getUsername(), MARIA_DB_CONTAINER.getPassword());
        guildDbModule.database.registerRowMapper(GuildDb.class, new GuildDbMapper());
    }

    @Test
    void get() {
        final long discordId = DISCORD_IDS.getAndIncrement();

        final Optional<GuildDb> guildNotFound = guildDbModule.get(discordId);
        assertThat(guildNotFound).isNotPresent();

        guildDbModule.create(discordId);

        final Optional<GuildDb> guildFound = guildDbModule.get(discordId);
        assertThat(guildFound).isPresent();
        assertThat(guildFound.get().getDiscordId()).isEqualTo(discordId);
    }

    @Test
    void getOrCreate() {
        final long discordId = DISCORD_IDS.getAndIncrement();

        final GuildDb guildDb = guildDbModule.getOrCreate(discordId);
        assertThat(guildDb.getDiscordId()).isEqualTo(discordId);
    }

    @Test
    void getCacheCheck() {
        final long discordId = DISCORD_IDS.getAndIncrement();

        guildDbModule.getOrCreate(discordId);
        final Optional<GuildDb> guildFound = guildDbModule.get(discordId);
        assertThat(guildFound).isPresent();
    }
}