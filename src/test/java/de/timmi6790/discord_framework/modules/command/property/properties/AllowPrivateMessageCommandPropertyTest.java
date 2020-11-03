package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllowPrivateMessageCommandPropertyTest {
    private CommandParameters getCommandParameters(final boolean isFromGuild) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
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
        final AllowPrivateMessageCommandProperty property = spy(new AllowPrivateMessageCommandProperty(false));
        final TestCommand command = new TestCommand();

        try (final MockedStatic<DiscordMessagesUtilities> discordMessageMock = mockStatic(DiscordMessagesUtilities.class)) {
            final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
            discordMessageMock.when(() -> DiscordMessagesUtilities.getEmbedBuilder(any())).thenReturn(embedBuilder);

            assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isTrue();
            assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isFalse();
        }
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