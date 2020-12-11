package de.timmi6790.discord_framework;

import de.timmi6790.commons.builders.SetBuilder;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.modules.AbstractModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DiscordBotTest {
    @Test
    void start_first_time() throws IOException, TopicalSortCycleException, InterruptedException, LoginException {
        final DiscordBot discordBot = spy(new DiscordBot());
        doReturn(new HashSet<>()).when(discordBot).getInternalModuleClasses();

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

    @Test
    @Disabled("Not working with github actions because of ModuleManager::getExternalModules")
    void start() throws IOException, TopicalSortCycleException, InterruptedException, LoginException {
        final DiscordBot discordBot = spy(new DiscordBot());
        doReturn(new Config()).when(discordBot).getConfig();
        doReturn(true).when(discordBot).setup();

        final Set<Class<? extends AbstractModule>> internalModuleClasses = SetBuilder.<Class<? extends AbstractModule>>ofHashSet()
                .add(TestModule.class)
                .build();
        doReturn(internalModuleClasses).when(discordBot).getInternalModuleClasses();

        try (final MockedStatic<JDABuilder> jdaMock = mockStatic(JDABuilder.class)) {
            final JDA jda = mock(JDA.class);

            final JDABuilder jdaBuilder = mock(JDABuilder.class);
            when(jdaBuilder.setStatus(any())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenReturn(jda);

            jdaMock.when(() -> JDABuilder.createLight(any(), anySet())).thenReturn(jdaBuilder);

            discordBot.start();
            assertThat(discordBot.getInternalModules()).hasSize(1);
        }
    }

    @Test
    @Disabled("Not working with github actions because of ModuleManager::getExternalModules")
    void start_check_module() throws IOException, TopicalSortCycleException, InterruptedException, LoginException {
        final DiscordBot discordBot = spy(new DiscordBot());
        doReturn(new Config()).when(discordBot).getConfig();
        doReturn(true).when(discordBot).setup();

        doReturn(new HashSet<>()).when(discordBot).getInternalModuleClasses();

        final TestModule module = spy(new TestModule());
        discordBot.getInternalModules().add(module);

        try (final MockedStatic<JDABuilder> jdaMock = mockStatic(JDABuilder.class)) {
            final JDA jda = mock(JDA.class);

            final JDABuilder jdaBuilder = mock(JDABuilder.class);
            when(jdaBuilder.setStatus(any())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenReturn(jda);

            jdaMock.when(() -> JDABuilder.createLight(any(), anySet())).thenReturn(jdaBuilder);

            discordBot.start();
            verify(module).onInitialize();
            verify(module).onEnable();
        }
    }

    public static class TestModule extends AbstractModule {
        public TestModule() {
            super("Test");
        }
    }
}