package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

class AllowBotCommandPropertyTest {
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
        final TestCommand command = new TestCommand();

        assertThat(property.onPermissionCheck(command, this.getCommandParameters(true))).isTrue();
        assertThat(property.onPermissionCheck(command, this.getCommandParameters(false))).isTrue();
    }

    @Test
    void onPermissionCheckDisAllow() {
        final AllowBotCommandProperty property = new AllowBotCommandProperty(false);
        final TestCommand command = new TestCommand();

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