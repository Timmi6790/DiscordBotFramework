package de.timmi6790.discord_framework.module.modules.channel;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Spy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChannelDbModuleTest {
    @Spy
    private static final GuildDbModule guildDbModule = spy(new GuildDbModule());
    @Spy
    private static final ChannelDbModule channelDbModule = spy(new ChannelDbModule());

    private static long createRandomId() {
        return ThreadLocalRandom.current().nextLong();
    }

    @BeforeAll
    static void setup() {
        final ModuleManager moduleManager = mock(ModuleManager.class);
        when(moduleManager.getModuleOrThrow(DatabaseModule.class)).thenReturn(AbstractIntegrationTest.databaseModule);
        when(moduleManager.getModuleOrThrow(GuildDbModule.class)).thenReturn(guildDbModule);
        when(moduleManager.getModuleOrThrow(ChannelDbModule.class)).thenReturn(channelDbModule);
        when(moduleManager.getModule(SettingModule.class)).thenReturn(Optional.empty());

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            final ShardManager discord = mock(ShardManager.class);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            guildDbModule.onInitialize();
            channelDbModule.onInitialize();
        }
    }

    @Test
    void get() {
        final long guildId = createRandomId();
        final long channelId = createRandomId();

        final Optional<ChannelDb> channelNotFound = channelDbModule.get(channelId);
        assertThat(channelNotFound).isEmpty();

        final ChannelDb channelFound = channelDbModule.create(channelId, guildId);

        assertThat(channelFound.getDiscordId()).isEqualTo(channelId);
        assertThat(channelFound.getGuildDb().getDiscordId()).isEqualTo(guildId);
    }


    @Test
    void getOrCreate() {
        final long guildId = createRandomId();
        final long channelId = createRandomId();

        final ChannelDb channelDbCreate = channelDbModule.getOrCreate(channelId, guildId);
        final ChannelDb channelDbCreate2 = channelDbModule.getOrCreate(channelId, guildId);

        assertThat(channelDbCreate.getDiscordId()).isEqualTo(channelDbCreate2.getDiscordId());
    }

    @SneakyThrows
    @Test
    void getOrCreate_multiple_threads() {
        final long guildId = createRandomId();
        final long channelId = createRandomId();

        final Supplier<ChannelDb> channelCreateTask = () -> channelDbModule.getOrCreate(channelId, guildId);
        final CompletableFuture<ChannelDb> channelDbFuture = CompletableFuture.supplyAsync(channelCreateTask);
        final CompletableFuture<ChannelDb> channelDbTwoFuture = CompletableFuture.supplyAsync(channelCreateTask);

        final ChannelDb channelDb = channelDbFuture.get();
        final ChannelDb channelDbTwo = channelDbTwoFuture.get();

        assertThat(channelDb).isEqualTo(channelDbTwo);
    }

    @Test
    void get_cache_check() {
        final long guildId = createRandomId();
        final long channelId = createRandomId();

        final ChannelDb channelDbCreate = channelDbModule.getOrCreate(channelId, guildId);

        final Optional<ChannelDb> noneCache = channelDbModule.get(channelId);
        assertThat(noneCache).isPresent();

        final Optional<ChannelDb> cache = channelDbModule.get(channelId);
        assertThat(cache).isPresent();

        channelDbModule.getCache().invalidate(channelId);
        final Optional<ChannelDb> channelDbDatabase = channelDbModule.get(channelId);
        assertThat(channelDbDatabase).isPresent();

        assertThat(channelDbCreate).isEqualTo(noneCache.get()).isEqualTo(channelDbDatabase.get());
    }
}