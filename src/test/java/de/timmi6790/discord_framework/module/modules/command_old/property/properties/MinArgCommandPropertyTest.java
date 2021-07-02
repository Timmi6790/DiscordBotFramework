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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class MinArgCommandPropertyTest {
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

    private CommandParameters getCommandParameters(final int length) {
        final String[] args = new String[length];
        for (int index = 0; length > index; index++) {
            args[index] = "A";
        }

        final CommandParameters commandParameters = Mockito.mock(CommandParameters.class);
        when(commandParameters.getArgs()).thenReturn(args);

        return commandParameters;
    }

    @Test
    void getValue() {
        final int input = 42;
        final MinArgCommandProperty property = new MinArgCommandProperty(input);
        assertThat(property.getValue()).isEqualTo(input);
    }

    @Test
    void onCommandExecutionTrue() {
        final int input = 42;
        final MinArgCommandProperty property = new MinArgCommandProperty(input);

        final TestCommand testCommand = this.createCommand();
        assertThat(property.onCommandExecution(testCommand, this.getCommandParameters(input))).isTrue();
        assertThat(property.onCommandExecution(testCommand, this.getCommandParameters(input + 1))).isTrue();
    }

    @Test
    void onCommandExecutionFalse() {
        final int input = 42;
        final MinArgCommandProperty property = new MinArgCommandProperty(input);

        final TestCommand testCommand = this.createCommand();
        assertThat(property.onCommandExecution(testCommand, this.getCommandParameters(input - 1))).isFalse();
    }

    private static class TestCommand extends AbstractCommand {
        public TestCommand() {
            super("", "", "", "");
        }

        @Override
        protected CommandResult onCommand(final CommandParameters commandParameters) {
            return null;
        }

        @Override
        public void sendMissingArgsMessage(final CommandParameters commandParameters) {
        }
    }
}