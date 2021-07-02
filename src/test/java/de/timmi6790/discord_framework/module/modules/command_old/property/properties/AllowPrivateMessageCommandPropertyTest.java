package de.timmi6790.discord_framework.module.modules.command_old.property.properties;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.command_old.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command_old.CommandModule;
import de.timmi6790.discord_framework.module.modules.command_old.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command_old.CommandResult;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllowPrivateMessageCommandPropertyTest {
    private TestCommand createCommand() {
        final ModuleManager moduleManager = mock(ModuleManager.class);

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getMainCommand()).thenReturn("");

        final EventModule eventModule = mock(EventModule.class);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(eventModule);

        final AchievementModule achievementModule = new AchievementModule();

        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);
        doReturn(achievementModule).when(moduleManager).getModuleOrThrow(AchievementModule.class);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);
            achievementModule.onInitialize();

            return new TestCommand();
        }
    }

    private CommandParameters getCommandParameters(final boolean isFromGuild) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(isFromGuild);

        return commandParameters;
    }

    @Test
    void getValueTrue() {
        final AllowPrivateMessageCommandProperty property = new AllowPrivateMessageCommandProperty(true);
        assertThat(property.getValue()).isTrue();
    }

    @Test
    void getValueFalse() {
        final AllowPrivateMessageCommandProperty property = new AllowPrivateMessageCommandProperty(false);
        assertThat(property.getValue()).isFalse();
    }

    @Test
    void onCommandExecutionTrue() {
        final AllowPrivateMessageCommandProperty property = new AllowPrivateMessageCommandProperty(true);

        final TestCommand command = this.createCommand();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isTrue();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isTrue();
    }

    @Test
    void onCommandExecutionFalse() {
        final AllowPrivateMessageCommandProperty property = spy(new AllowPrivateMessageCommandProperty(false));
        final TestCommand command = this.createCommand();

        try (final MockedStatic<DiscordMessagesUtilities> discordMessageMock = mockStatic(DiscordMessagesUtilities.class)) {
            final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
            discordMessageMock.when(() -> DiscordMessagesUtilities.getEmbedBuilder(any())).thenReturn(embedBuilder);

            assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isTrue();
            assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isFalse();
        }
    }

    public static class TestCommand extends AbstractCommand {
        public TestCommand() {
            super("Test", "", "", "");
        }

        @Override
        protected CommandResult onCommand(final CommandParameters commandParameters) {
            return null;
        }
    }
}