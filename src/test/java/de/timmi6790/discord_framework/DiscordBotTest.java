package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import static org.mockito.Mockito.*;

class DiscordBotTest {
    @Test
    void start_first_time() throws IOException, TopicalSortCycleException, InterruptedException, LoginException {
        final DiscordBot discordBot = spy(new DiscordBot());

        //TODO: The test is not stable in this env, it exists after the first run
        try (final MockedStatic<DefaultShardManagerBuilder> jdaMock = mockStatic(DefaultShardManagerBuilder.class)) {
            final ShardManager jda = mock(ShardManager.class);

            final DefaultShardManagerBuilder jdaBuilder = mock(DefaultShardManagerBuilder.class);
            when(jdaBuilder.setStatus(any())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenReturn(jda);

            jdaMock.when(() -> DefaultShardManagerBuilder.createLight(any(), anySet())).thenReturn(jdaBuilder);

            discordBot.start();
            verify(jda, never()).shutdown();
        }
    }
}