package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CooldownCommandPropertyTest {
    private void runTest(final long durationInSeconds, final boolean secondAssert) {
        final TestCommand command = new TestCommand();
        final CooldownCommandProperty property = new CooldownCommandProperty(durationInSeconds, TimeUnit.SECONDS);

        final UserDb userDb = mock(UserDb.class);
        when(userDb.getDiscordId()).thenReturn(1L);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);

        try (final MockedStatic<DiscordMessagesUtilities> discordMessageMock = mockStatic(DiscordMessagesUtilities.class)) {
            final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
            discordMessageMock.when(() -> DiscordMessagesUtilities.getEmbedBuilder(any())).thenReturn(embedBuilder);

            assertThat(property.onCommandExecution(command, commandParameters)).isTrue();
            assertThat(property.onCommandExecution(command, commandParameters)).isEqualTo(secondAssert);
        }
    }

    @Test
    void onCommandExecution() {
        this.runTest(TimeUnit.DAYS.toSeconds(1), false);
    }

    @Test
    void onCommandExecution_expired() {
        this.runTest(0, true);

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