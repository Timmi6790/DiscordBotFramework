package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class AllowGuildMessageCommandPropertyTest {
    private CommandParameters getCommandParameters(final boolean isFromGuild) {
        final CommandParameters commandParameters = Mockito.mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(isFromGuild);

        return commandParameters;
    }

    @Test
    void getValueTrue() {
        final AllowGuildMessageCommandProperty property = new AllowGuildMessageCommandProperty(true);
        assertThat(property.getValue()).isTrue();
    }

    @Test
    void getValueFalse() {
        final AllowGuildMessageCommandProperty property = new AllowGuildMessageCommandProperty(false);
        assertThat(property.getValue()).isFalse();
    }

    @Test
    void onCommandExecutionTrue() {
        final AllowGuildMessageCommandProperty property = new AllowGuildMessageCommandProperty(true);

        final TestCommand command = new TestCommand();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isTrue();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isTrue();
    }

    @Test
    void onCommandExecutionFalse() {
        final AllowGuildMessageCommandProperty property = Mockito.spy(new AllowGuildMessageCommandProperty(false));
        doNothing().when(property).sendErrorMessage(Mockito.any());

        final TestCommand command = new TestCommand();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isFalse();
        assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isTrue();
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