package de.timmi6790.discord_framework.module.modules.command.property.properties;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class AllowBotCommandPropertyTest {
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

    private CommandParameters getCommandParameters(final boolean isBot) {
        final User user = Mockito.mock(User.class);
        when(user.isBot()).thenReturn(isBot);

        final CommandParameters commandParameters = Mockito.mock(CommandParameters.class);
        when(commandParameters.getUser()).thenReturn(user);

        return commandParameters;
    }

    @Test
    void getValueTrue() {
        final AllowBotCommandProperty property = new AllowBotCommandProperty(true);
        assertThat(property.getValue()).isTrue();
    }

    @Test
    void getValueFalse() {
        final AllowBotCommandProperty property = new AllowBotCommandProperty(false);
        assertThat(property.getValue()).isFalse();
    }

    @Test
    void onPermissionCheckAllow() {
        final AllowBotCommandProperty property = new AllowBotCommandProperty(true);
        final TestCommand command = this.createCommand();

        assertThat(property.onPermissionCheck(command, this.getCommandParameters(true))).isTrue();
        assertThat(property.onPermissionCheck(command, this.getCommandParameters(false))).isTrue();
    }

    @Test
    void onPermissionCheckDisAllow() {
        final AllowBotCommandProperty property = new AllowBotCommandProperty(false);
        final TestCommand command = this.createCommand();

        assertThat(property.onPermissionCheck(command, this.getCommandParameters(false))).isTrue();
        assertThat(property.onPermissionCheck(command, this.getCommandParameters(true))).isFalse();
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