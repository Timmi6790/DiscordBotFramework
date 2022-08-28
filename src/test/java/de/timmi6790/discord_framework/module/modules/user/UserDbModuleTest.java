package de.timmi6790.discord_framework.module.modules.user;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class UserDbModuleTest {
    private static final AtomicLong ID = new AtomicLong(0);

    private static final UserDbModule USER_DB_MODULE = spy(new UserDbModule());
    private static final EventModule EVENT_MODULE = new EventModule();
    private static final RankModule RANK_MODULE = spy(new RankModule());

    private long createRandomId() {
        return ID.incrementAndGet();
    }

    @BeforeAll
    static void setUp() {
        final ModuleManager moduleManager = mock(ModuleManager.class);

        final SlashCommandModule commandModule = spy(new SlashCommandModule());
        doNothing().when(commandModule).registerCommands(any(), any());

        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);
        when(moduleManager.getModuleOrThrow(SlashCommandModule.class)).thenReturn(commandModule);
        when(moduleManager.getModuleOrThrow(RankModule.class)).thenReturn(RANK_MODULE);
        when(moduleManager.getModuleOrThrow(UserDbModule.class)).thenReturn(USER_DB_MODULE);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(EVENT_MODULE);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            final ShardManager discord = mock(ShardManager.class);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            RANK_MODULE.onInitialize();
            USER_DB_MODULE.onInitialize();
        }
    }

    @Test
    void get() {
        final long userId = this.createRandomId();
        final Optional<UserDb> userDbNotFound = USER_DB_MODULE.get(userId);
        assertThat(userDbNotFound).isNotPresent();

        USER_DB_MODULE.create(userId);
        final Optional<UserDb> userDbFound = USER_DB_MODULE.get(userId);
        assertThat(userDbFound).isPresent();
        assertThat(userDbFound.get().getDiscordId()).isEqualTo(userId);
    }

    @Test
    void getOrCreate() {
        final long userId = this.createRandomId();
        // Should create them
        final UserDb userDb = USER_DB_MODULE.getOrCreate(userId);
        assertThat(userDb).isNotNull();
        assertThat(userDb.getDiscordId()).isEqualTo(userId);

        // Should get it without creation
        final UserDb userDb2 = USER_DB_MODULE.getOrCreate(userId);
        assertThat(userDb2).isNotNull();
        assertThat(userDb2.getDiscordId()).isEqualTo(userId);
    }

    @Test
    void deleteUser() {
        final long userId = this.createRandomId();
        final UserDb userDb = USER_DB_MODULE.getOrCreate(userId);
        USER_DB_MODULE.delete(userDb);

        final Optional<UserDb> deletedUser = USER_DB_MODULE.get(userId);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void deleteId() {
        final long userId = this.createRandomId();
        final UserDb userDb = USER_DB_MODULE.getOrCreate(userId);
        USER_DB_MODULE.delete(userDb.getDiscordId());

        final Optional<UserDb> deletedUser = USER_DB_MODULE.get(userId);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void checkIncorrectCache() {
        final long userId = this.createRandomId();
        USER_DB_MODULE.create(userId);
        final Optional<UserDb> cachedUser = USER_DB_MODULE.getFromCache(userId);

        USER_DB_MODULE.invalidateCache(userId);
        final UserDb dbUser = USER_DB_MODULE.getOrCreate(userId);

        assertThat(cachedUser)
                .isPresent()
                .contains(dbUser);
    }

    @SneakyThrows
    @Test
    void getOrCreate_multiple_threads() {
        final long guildId = this.createRandomId();

        final Supplier<UserDb> guildCreateTask = () -> USER_DB_MODULE.getOrCreate(guildId);
        final CompletableFuture<UserDb> guildDbFuture = CompletableFuture.supplyAsync(guildCreateTask);
        final CompletableFuture<UserDb> guildDbTwoFuture = CompletableFuture.supplyAsync(guildCreateTask);

        final UserDb guildDb = guildDbFuture.get();
        final UserDb guildDbTwo = guildDbTwoFuture.get();

        Assertions.assertThat(guildDb).isEqualTo(guildDbTwo);
    }
}