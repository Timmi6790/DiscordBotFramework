package de.timmi6790.discord_framework.modules.channel;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class ChannelDbModuleTest {
    private static final long TEST_GUILD_ID = 405911488697204736L;

    private static final long TEST_CHANNEL_ID = 305911488697204736L;
    private static final long TEST_CHANNEL_ID2 = 168049519831810048L;
    private static final long TEST_CHANNEL_ID3 = 308911488647204736L;

    @Spy
    private static final GuildDbModule guildDbModule = Mockito.spy(new GuildDbModule());
    @Spy
    private static final ChannelDbModule channelDbModule = Mockito.spy(new ChannelDbModule());

    @BeforeAll
    static void setup() {
        doReturn(AbstractIntegrationTest.databaseModule).when(guildDbModule).getModuleOrThrow(DatabaseModule.class);

        doReturn(AbstractIntegrationTest.databaseModule).when(channelDbModule).getModuleOrThrow(DatabaseModule.class);
        doReturn(guildDbModule).when(channelDbModule).getModuleOrThrow(GuildDbModule.class);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);

            final JDA discord = mock(JDA.class);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            guildDbModule.onInitialize();
            channelDbModule.onInitialize();
        }

        // We need this to exist;
        guildDbModule.getOrCreate(TEST_GUILD_ID);
    }

    @Test
    void get() {
        final Optional<ChannelDb> channelNotFound = channelDbModule.get(TEST_CHANNEL_ID);
        assertThat(channelNotFound).isEmpty();

        final ChannelDb channelFound = channelDbModule.create(TEST_CHANNEL_ID, TEST_GUILD_ID);

        assertThat(channelFound.getDiscordId()).isEqualTo(TEST_CHANNEL_ID);
        assertThat(channelFound.getGuildDb().getDiscordId()).isEqualTo(TEST_GUILD_ID);
    }


    @Test
    void getOrCreate() {
        final ChannelDb channelDbCreate = channelDbModule.getOrCreate(TEST_CHANNEL_ID2, TEST_GUILD_ID);
        final ChannelDb channelDbCreate2 = channelDbModule.getOrCreate(TEST_CHANNEL_ID2, TEST_GUILD_ID);

        assertThat(channelDbCreate.getDiscordId()).isEqualTo(channelDbCreate2.getDiscordId());
        assertThat(channelDbCreate.getDatabaseId()).isEqualTo(channelDbCreate2.getDatabaseId());
    }

    @Test
    void get_cache_check() {
        final ChannelDb channelDbCreate = channelDbModule.getOrCreate(TEST_CHANNEL_ID3, TEST_GUILD_ID);

        final Optional<ChannelDb> noneCache = channelDbModule.get(TEST_CHANNEL_ID3);
        assertThat(noneCache).isPresent();

        final Optional<ChannelDb> cache = channelDbModule.get(TEST_CHANNEL_ID3);
        assertThat(cache).isPresent();

        channelDbModule.getCache().invalidate(TEST_CHANNEL_ID3);
        final Optional<ChannelDb> channelDbDatabase = channelDbModule.get(TEST_CHANNEL_ID3);
        assertThat(channelDbDatabase).isPresent();

        assertThat(channelDbCreate).isEqualTo(noneCache.get()).isEqualTo(channelDbDatabase.get());
    }
}