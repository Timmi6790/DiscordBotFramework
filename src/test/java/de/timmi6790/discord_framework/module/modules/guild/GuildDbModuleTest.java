package de.timmi6790.discord_framework.module.modules.guild;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class GuildDbModuleTest {
    private static final AtomicLong DISCORD_IDS = new AtomicLong(0);
    @Spy
    private static final GuildDbModule guildDbModule = Mockito.spy(new GuildDbModule());

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
        final long discordId = DISCORD_IDS.getAndIncrement();

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
        final long discordId = DISCORD_IDS.getAndIncrement();

        final GuildDb guildDbCreate = guildDbModule.getOrCreate(discordId);

        final Optional<GuildDb> guildDbCache = guildDbModule.get(discordId);
        assertThat(guildDbCache).isPresent();

        guildDbModule.getCache().invalidate(discordId);
        final Optional<GuildDb> guildldDatabase = guildDbModule.get(discordId);
        assertThat(guildldDatabase).isPresent();

        AssertionsForClassTypes.assertThat(guildDbCreate.getDiscordId())
                .isEqualTo(guildDbCache.get().getDiscordId())
                .isEqualTo(guildldDatabase.get().getDiscordId());
    }
}