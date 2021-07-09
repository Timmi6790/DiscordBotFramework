package de.timmi6790.discord_framework.module.modules.command.property;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandPropertyTest {
    @Test
    void onPermissionCheck() {
        final Command command = mock(Command.class);
        final CommandParameters commandParameters = mock(CommandParameters.class);

        final CommandProperty<?> property = mock(CommandProperty.class);
        when(property.onPermissionCheck(command, commandParameters)).thenCallRealMethod();

        final boolean returnValue = property.onPermissionCheck(command, commandParameters);
        assertThat(returnValue).isTrue();
    }

    @Test
    void onCommandExecution() {
        final Command command = mock(Command.class);
        final CommandParameters commandParameters = mock(CommandParameters.class);

        final CommandProperty<?> property = mock(CommandProperty.class);
        when(property.onCommandExecution(command, commandParameters)).thenCallRealMethod();

        final boolean returnValue = property.onCommandExecution(command, commandParameters);
        assertThat(returnValue).isTrue();
    }
}