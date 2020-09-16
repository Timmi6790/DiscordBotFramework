package de.timmi6790.discord_framework.modules.channel;

import de.timmi6790.discord_framework.fake_modules.FakeDatabaseModel;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class ChannelDbModuleTest {
    private static final long TEST_GUILD_ID = 405911488697204736L;

    private static final long TEST_CHANNEL_ID = 305911488697204736L;
    private static final long TEST_CHANNEL_ID2 = 168049519831810048L;
    private static final long TEST_CHANNEL_ID3 = 308911488647204736L;

    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = (MariaDBContainer) new MariaDBContainer().withClasspathResourceMapping(
            "tables.sql",
            "/docker-entrypoint-initdb.d/createTables.sql",
            BindMode.READ_ONLY
    );

    @Spy
    private final GuildDbModule guildDbModule = new GuildDbModule();
    @Spy
    private final ChannelDbModule channelDbModule = new ChannelDbModule();

    @BeforeEach
    void setup() {
        final FakeDatabaseModel fakeDatabaseModel = new FakeDatabaseModel(MARIA_DB_CONTAINER);

        doReturn(fakeDatabaseModel).when(this.guildDbModule).getModuleOrThrow(DatabaseModule.class);
        this.guildDbModule.onInitialize();

        doReturn(fakeDatabaseModel).when(this.channelDbModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(this.guildDbModule).when(this.channelDbModule).getModuleOrThrow(GuildDbModule.class);

        this.channelDbModule.onInitialize();

        // We need this to exist;
        this.guildDbModule.getOrCreate(TEST_GUILD_ID);
    }

    @Test
    void get() {
        final Optional<ChannelDb> channelNotFound = this.channelDbModule.get(TEST_CHANNEL_ID);
        assertThat(channelNotFound).isEmpty();

        final ChannelDb channelFound = this.channelDbModule.create(TEST_CHANNEL_ID, TEST_GUILD_ID);

        assertThat(channelFound.getDiscordId()).isEqualTo(TEST_CHANNEL_ID);
        assertThat(channelFound.getGuildDb().getDiscordId()).isEqualTo(TEST_GUILD_ID);
    }


    @Test
    void getOrCreate() {
        final ChannelDb channelDbCreate = this.channelDbModule.getOrCreate(TEST_CHANNEL_ID2, TEST_GUILD_ID);
        final ChannelDb channelDbCreate2 = this.channelDbModule.getOrCreate(TEST_CHANNEL_ID2, TEST_GUILD_ID);

        assertThat(channelDbCreate.getDiscordId()).isEqualTo(channelDbCreate2.getDiscordId());
        assertThat(channelDbCreate.getDatabaseId()).isEqualTo(channelDbCreate2.getDatabaseId());
    }

    @Test
    void getCacheCheck() {
        final ChannelDb channelDbCreate = this.channelDbModule.getOrCreate(TEST_CHANNEL_ID3, TEST_GUILD_ID);

        final Optional<ChannelDb> channelDbCache = this.channelDbModule.get(TEST_CHANNEL_ID3);
        assertThat(channelDbCache).isPresent();

        this.channelDbModule.getCache().invalidate(TEST_CHANNEL_ID3);
        final Optional<ChannelDb> channelDbDatabase = this.channelDbModule.get(TEST_CHANNEL_ID3);
        assertThat(channelDbDatabase).isPresent();

        assertThat(channelDbCreate).isEqualTo(channelDbCache.get()).isEqualTo(channelDbDatabase.get());
    }
}