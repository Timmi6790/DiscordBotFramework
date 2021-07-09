package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AllowBotPropertyTest {
    private void onPermissionCheck(final boolean expectedReturnValue, final boolean allowedBot, final boolean isBot) {
        final User user = mock(User.class);
        when(user.isBot()).thenReturn(isBot);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUser()).thenReturn(user);

        final Command command = mock(Command.class);

        final AllowBotProperty property = new AllowBotProperty(allowedBot);
        final boolean returnValue = property.onPermissionCheck(command, commandParameters);
        assertThat(returnValue).isEqualTo(expectedReturnValue);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getValue(final boolean allowedBot) {
        final AllowBotProperty property = new AllowBotProperty(allowedBot);
        assertThat(property.getValue()).isEqualTo(allowedBot);
    }

    @Test
    void onPermissionCheck_allow_is_bot() {
        this.onPermissionCheck(true, true, true);
    }

    @Test
    void onPermissionCheck_allow_is_user() {
        this.onPermissionCheck(true, true, false);
    }

    @Test
    void onPermissionCheck_forbidden_is_bot() {
        this.onPermissionCheck(false, false, true);
    }

    @Test
    void onPermissionCheck_forbidden_is_user() {
        this.onPermissionCheck(true, false, false);
    }
}