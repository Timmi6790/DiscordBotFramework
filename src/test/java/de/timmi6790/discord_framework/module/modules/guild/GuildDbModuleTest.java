package de.timmi6790.discord_framework.module.modules.guild;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GuildDbModuleTest {
    @Spy
    private static final GuildDbModule guildDbModule = Mockito.spy(new GuildDbModule());

    private static long createRandomId() {
        return ThreadLocalRandom.current().nextLong();
    }

    @BeforeAll
    static void setUp() {
        final ModuleManager moduleManager = mock(ModuleManager.class);
        when(moduleManager.getModuleOrThrow(DatabaseModule.class)).thenReturn(AbstractIntegrationTest.databaseModule);
        when(moduleManager.getModuleOrThrow(GuildDbModule.class)).thenReturn(guildDbModule);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            final ShardManager discord = mock(ShardManager.class);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            guildDbModule.onInitialize();
        }
    }

    @Test
    void get() {
        final long discordId = createRandomId();

        final Optional<GuildDb> guildNotFound = guildDbModule.get(discordId);
        assertThat(guildNotFound).isNotPresent();

        guildDbModule.create(discordId);

        final Optional<GuildDb> guildFoundOpt = guildDbModule.get(discordId);
        assertThat(guildFoundOpt).isPresent();

        final GuildDb guild = guildFoundOpt.get();
        assertThat(guild.getDiscordId()).isEqualTo(discordId);
        assertThat(guild.isBanned()).isFalse();
    }

    @Test
    void getCacheCheck() {
        final long discordId = createRandomId();

        final GuildDb guildDbCreate = guildDbModule.getOrCreate(discordId);

        final Optional<GuildDb> guildDbCache = guildDbModule.get(discordId);
        assertThat(guildDbCache).isPresent();

        guildDbModule.getCache().invalidate(discordId);
        final Optional<GuildDb> guildldDatabase = guildDbModule.get(discordId);
        assertThat(guildldDatabase).isPresent();

        assertThat(guildDbCreate.getDiscordId())
                .isEqualTo(guildDbCache.get().getDiscordId())
                .isEqualTo(guildldDatabase.get().getDiscordId());
    }

    @Test
    void getOrCreate() {
        final long guildId = createRandomId();

        final GuildDb guildDbCreate = guildDbModule.getOrCreate(guildId);
        final GuildDb guildDbCreate2 = guildDbModule.getOrCreate(guildId);

        assertThat(guildDbCreate.getDiscordId()).isEqualTo(guildDbCreate2.getDiscordId());
    }

    @SneakyThrows
    @Test
    void getOrCreate_multiple_threads() {
        final long guildId = createRandomId();

        final Supplier<GuildDb> guildCreateTask = () -> guildDbModule.getOrCreate(guildId);
        final CompletableFuture<GuildDb> guildDbFuture = CompletableFuture.supplyAsync(guildCreateTask);
        final CompletableFuture<GuildDb> guildDbTwoFuture = CompletableFuture.supplyAsync(guildCreateTask);

        final GuildDb guildDb = guildDbFuture.get();
        final GuildDb guildDbTwo = guildDbTwoFuture.get();

        assertThat(guildDb).isEqualTo(guildDbTwo);
    }
}