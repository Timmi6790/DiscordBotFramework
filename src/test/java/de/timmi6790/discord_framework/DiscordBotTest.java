package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import static org.mockito.Mockito.*;

class DiscordBotTest {
    @Test
    void start_first_time() throws IOException, TopicalSortCycleException, InterruptedException, LoginException {
        final DiscordBot discordBot = spy(new DiscordBot());

        try (final MockedStatic<JDABuilder> jdaMock = mockStatic(JDABuilder.class)) {
            final JDA jda = mock(JDA.class);

            final JDABuilder jdaBuilder = mock(JDABuilder.class);
            when(jdaBuilder.setStatus(any())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenReturn(jda);

            jdaMock.when(() -> JDABuilder.createLight(any(), anySet())).thenReturn(jdaBuilder);

            discordBot.start();
            verify(jda, never()).awaitReady();
        }
    }
}