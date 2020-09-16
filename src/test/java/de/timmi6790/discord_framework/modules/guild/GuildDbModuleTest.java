package de.timmi6790.discord_framework.modules.guild;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class GuildDbModuleTest {
    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = (MariaDBContainer) new MariaDBContainer().withClasspathResourceMapping(
            "tables.sql",
            "/docker-entrypoint-initdb.d/createTables.sql",
            BindMode.READ_ONLY
    );
    private static final AtomicLong DISCORD_IDS = new AtomicLong(0);
    @Spy
    private final GuildDbModule guildDbModule = new GuildDbModule();

    @BeforeEach
    void setUp() {
        final FakeDatabaseModel fakeDatabaseModel = new FakeDatabaseModel(MARIA_DB_CONTAINER);

        doReturn(fakeDatabaseModel).when(this.guildDbModule).getModuleOrThrow(DatabaseModule.class);
        this.guildDbModule.onInitialize();
    }

    @Test
    void get() {
        final long discordId = DISCORD_IDS.getAndIncrement();

        final Optional<GuildDb> guildNotFound = this.guildDbModule.get(discordId);
        assertThat(guildNotFound).isNotPresent();

        this.guildDbModule.create(discordId);

        final Optional<GuildDb> guildFound = this.guildDbModule.get(discordId);
        assertThat(guildFound).isPresent();
    }

    @Test
    void getCacheCheck() {
        final long discordId = DISCORD_IDS.getAndIncrement();

        final GuildDb guildDbCreate = this.guildDbModule.getOrCreate(discordId);

        final Optional<GuildDb> guildDbCache = this.guildDbModule.get(discordId);
        assertThat(guildDbCache).isPresent();

        this.guildDbModule.getCache().invalidate(discordId);
        final Optional<GuildDb> guildldDatabase = this.guildDbModule.get(discordId);
        assertThat(guildldDatabase).isPresent();

        AssertionsForClassTypes.assertThat(guildDbCreate.getDatabaseId())
                .isEqualTo(guildDbCache.get().getDatabaseId())
                .isEqualTo(guildldDatabase.get().getDatabaseId());
    }
}