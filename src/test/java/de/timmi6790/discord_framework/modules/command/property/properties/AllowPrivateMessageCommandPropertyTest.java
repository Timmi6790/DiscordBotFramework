package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class AllowPrivateMessageCommandPropertyTest {
    private CommandParameters getCommandParameters(final boolean isFromGuild) {
        final CommandParameters commandParameters = Mockito.mock(CommandParameters.class);
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

        final TestCommand command = new TestCommand();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isTrue();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isTrue();
    }

    @Test
    void onCommandExecutionFalse() {
        final AllowPrivateMessageCommandProperty property = Mockito.spy(new AllowPrivateMessageCommandProperty(false));
        doNothing().when(property).sendErrorMessage(Mockito.any());

        final TestCommand command = new TestCommand();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isTrue();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isFalse();
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