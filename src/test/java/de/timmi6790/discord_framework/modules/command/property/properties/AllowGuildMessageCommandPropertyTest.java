package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllowGuildMessageCommandPropertyTest {
    private CommandParameters getCommandParameters(final boolean isFromGuild) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
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
        final AllowGuildMessageCommandProperty property = spy(new AllowGuildMessageCommandProperty(false));
        final TestCommand command = new TestCommand();

        try (final MockedStatic<DiscordMessagesUtilities> discordMessageMock = mockStatic(DiscordMessagesUtilities.class)) {
            final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
            discordMessageMock.when(() -> DiscordMessagesUtilities.getEmbedBuilder(any())).thenReturn(embedBuilder);

            assertThat(property.onCommandExecution(command, this.getCommandParameters(true))).isFalse();
            assertThat(property.onCommandExecution(command, this.getCommandParameters(false))).isTrue();
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