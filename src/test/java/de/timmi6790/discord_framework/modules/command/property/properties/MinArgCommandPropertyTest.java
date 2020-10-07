package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

class MinArgCommandPropertyTest {
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

        final TestCommand testCommand = new TestCommand();
        assertThat(property.onCommandExecution(testCommand, this.getCommandParameters(input))).isTrue();
        assertThat(property.onCommandExecution(testCommand, this.getCommandParameters(input + 1))).isTrue();
    }

    @Test
    void onCommandExecutionFalse() {
        final int input = 42;
        final MinArgCommandProperty property = new MinArgCommandProperty(input);

        final TestCommand testCommand = new TestCommand();
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