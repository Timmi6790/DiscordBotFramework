package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllowGuildMessagePropertyTest {
    void onCommandExecution(final boolean expectedReturnValue, final boolean guildCommand, final boolean allowGuildMessage) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(guildCommand);
        when(commandParameters.getEmbedBuilder()).thenReturn(new MultiEmbedBuilder());

        final Command command = mock(Command.class);

        final AllowGuildMessageProperty property = spy(new AllowGuildMessageProperty(allowGuildMessage));

        final boolean returnValue = property.onCommandExecution(command, commandParameters);
        assertThat(returnValue).isEqualTo(expectedReturnValue);

        // Verify that the error method message is called and a message is send when the return value is false
        if (!expectedReturnValue) {
            verify(property).sendErrorMessage(any());
            verify(commandParameters).sendMessage(any());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getValue(final boolean allowGuildMessage) {
        final AllowGuildMessageProperty property = new AllowGuildMessageProperty(allowGuildMessage);
        assertThat(property.getValue()).isEqualTo(allowGuildMessage);
    }

    @Test
    void onCommandExecution_allow_is_guild() {
        this.onCommandExecution(true, true, true);
    }

    @Test
    void onCommandExecution_allow_is_private_message() {
        this.onCommandExecution(true, false, true);
    }

    @Test
    void onCommandExecution_disallow_is_guild() {
        this.onCommandExecution(false, true, false);
    }

    @Test
    void onCommandExecution_disallow_is_private_message() {
        this.onCommandExecution(true, false, false);
    }
}