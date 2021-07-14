package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CooldownPropertyTest {
    private static final AtomicInteger USER_ID = new AtomicInteger(0);

    private long getUserId() {
        return USER_ID.incrementAndGet();
    }

    private CommandParameters getCommandParameters(final long userId) {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.getDiscordId()).thenReturn(userId);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getEmbedBuilder()).thenReturn(new MultiEmbedBuilder());
        when(commandParameters.getUserDb()).thenReturn(userDb);

        return commandParameters;
    }

    @Test
    void getValue() {
        final CooldownProperty property = new CooldownProperty(1, TimeUnit.MINUTES);
        // This always returns true
        assertThat(property.getValue()).isTrue();
    }

    @Test
    void constructor_zero_duration() {
        assertThatThrownBy(() ->
                new CooldownProperty(0, TimeUnit.MINUTES)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_negative_duration() {
        assertThatThrownBy(() ->
                new CooldownProperty(-1, TimeUnit.MINUTES)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onCommandExecution_same_id() {
        final long userId = this.getUserId();
        final CommandParameters commandParameters = this.getCommandParameters(userId);

        final Command command = mock(Command.class);

        final CooldownProperty property = spy(new CooldownProperty(1, TimeUnit.MINUTES));

        final boolean returnValue = property.onCommandExecution(command, commandParameters);
        assertThat(returnValue).isTrue();

        final boolean secondReturnValue = property.onCommandExecution(command, commandParameters);
        assertThat(secondReturnValue).isFalse();

        // Verify that the error method message is called and a message is send when the return value is false
        verify(property).sendErrorMessage(eq(commandParameters), anyLong());
        verify(commandParameters).sendMessage(any());
    }

    @Test
    void onCommandExecution_different_users() {
        final Command command = mock(Command.class);
        final CooldownProperty property = new CooldownProperty(1, TimeUnit.MINUTES);

        for (int count = 0; 10 >= count; count++) {
            final long userId = this.getUserId();
            final CommandParameters commandParameters = this.getCommandParameters(userId);

            final boolean returnValue = property.onCommandExecution(command, commandParameters);
            assertThat(returnValue).isTrue();
        }
    }

    @SneakyThrows
    @Test
    void onCommandExecution_expire() {
        final long userId = this.getUserId();
        final CommandParameters commandParameters = this.getCommandParameters(userId);

        final Command command = mock(Command.class);

        final CooldownProperty property = spy(new CooldownProperty(1, TimeUnit.NANOSECONDS));
        final boolean firstReturnValue = property.onCommandExecution(command, commandParameters);
        assertThat(firstReturnValue).isTrue();

        // waiting for one millisecond is enough to let the 1 nano cache expire
        Thread.sleep(1);

        final boolean secondReturnValue = property.onCommandExecution(command, commandParameters);
        assertThat(secondReturnValue).isTrue();
    }

    @Test
    void getFormattedTime_full() {
        final CooldownProperty property = new CooldownProperty(1, TimeUnit.HOURS);

        long timeInSeconds = 0;
        timeInSeconds += TimeUnit.HOURS.toSeconds(1);
        timeInSeconds += TimeUnit.MINUTES.toSeconds(9);
        timeInSeconds += 7;

        final String response = property.getFormattedTime(timeInSeconds);
        assertThat(response).isEqualTo("1 hours 9 minutes 7 seconds");
    }

    @Test
    void getFormattedTime_only_hours() {
        final CooldownProperty property = new CooldownProperty(1, TimeUnit.HOURS);
        final String response = property.getFormattedTime(TimeUnit.HOURS.toSeconds(10));
        assertThat(response).isEqualTo("10 hours");
    }

    @Test
    void getFormattedTime_only_minutes() {
        final CooldownProperty property = new CooldownProperty(1, TimeUnit.HOURS);
        final String response = property.getFormattedTime(TimeUnit.MINUTES.toSeconds(59));
        assertThat(response).isEqualTo("59 minutes");
    }

    @Test
    void getFormattedTime_only_seconds() {
        final CooldownProperty property = new CooldownProperty(1, TimeUnit.HOURS);
        final String response = property.getFormattedTime(10);
        assertThat(response).isEqualTo("10 seconds");
    }
}