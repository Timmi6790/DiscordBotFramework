package de.timmi6790.discord_framework.modules.guild;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class GuildDbModuleTest {
    private static final AtomicLong DISCORD_IDS = new AtomicLong(0);
    @Spy
    private static final GuildDbModule guildDbModule = Mockito.spy(GuildDbModule.class);

    @BeforeAll
    static void setUp() {
   
    }

    @Test
    void get() {
        final long discordId = DISCORD_IDS.getAndIncrement();

        final Optional<GuildDb> guildNotFound = guildDbModule.get(discordId);
        assertThat(guildNotFound).isNotPresent();

        guildDbModule.create(discordId);

        final Optional<GuildDb> guildFound = guildDbModule.get(discordId);
        assertThat(guildFound).isPresent();
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

        AssertionsForClassTypes.assertThat(guildDbCreate.getDatabaseId())
                .isEqualTo(guildDbCache.get().getDatabaseId())
                .isEqualTo(guildldDatabase.get().getDatabaseId());
    }
}