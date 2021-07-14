package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.utilities.MessageUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MinArgPropertyTest {
    void onCommandExecution(final boolean expectedReturnValue, final int minArgs, final int argsLength) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArgs()).thenReturn(new String[argsLength]);

        final Command command = mock(Command.class);
        
        final MinArgProperty property = spy(new MinArgProperty(minArgs));

        final boolean returnValue;
        // We only need to mock it when the expected value is false
        if (!expectedReturnValue) {
            try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
                returnValue = property.onCommandExecution(command, commandParameters);
            }
        } else {
            returnValue = property.onCommandExecution(command, commandParameters);
        }
        assertThat(returnValue).isEqualTo(expectedReturnValue);
    }

    @ParameterizedTest
    @ValueSource(bytes = {1, 42, 127})
    void getValue(final byte minArgs) {
        final MinArgProperty property = new MinArgProperty(minArgs);
        assertThat(property.getValue()).isEqualTo(minArgs);
    }

    @Test
    void constructor_zero_duration() {
        assertThatThrownBy(() ->
                new MinArgProperty(0)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_above_byte() {
        assertThatThrownBy(() ->
                new MinArgProperty(128)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onCommandExecution_same_length() {
        this.onCommandExecution(
                true,
                1,
                1
        );
    }

    @Test
    void onCommandExecution_bigger_length() {
        this.onCommandExecution(
                true,
                10,
                11
        );
    }

    @Test
    void onCommandExecution_smaller_length() {
        this.onCommandExecution(
                false,
                30,
                2
        );
    }
}