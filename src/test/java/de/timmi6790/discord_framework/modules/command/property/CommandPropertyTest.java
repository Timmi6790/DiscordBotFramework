package de.timmi6790.discord_framework.modules.command.property;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CommandPropertyTest {
    @Test
    void onPermissionCheckDefaultResponse() {
        final AbstractCommand command = mock(AbstractCommand.class);
        final CommandParameters commandParameters = mock(CommandParameters.class);

        final TestCommandProperty property = new TestCommandProperty();
        assertThat(property.onPermissionCheck(command, commandParameters)).isTrue();
    }

    @Test
    void onCommandExecutionDefaultResponse() {
        final AbstractCommand command = mock(AbstractCommand.class);
        final CommandParameters commandParameters = mock(CommandParameters.class);

        final TestCommandProperty property = new TestCommandProperty();
        assertThat(property.onCommandExecution(command, commandParameters)).isTrue();
    }

    private static class TestCommandProperty implements CommandProperty<Boolean> {
        @Override
        public Boolean getValue() {
            return false;
        }
    }
}