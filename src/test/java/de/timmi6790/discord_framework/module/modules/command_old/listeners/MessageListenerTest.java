package de.timmi6790.discord_framework.module.modules.command_old.listeners;

import de.timmi6790.discord_framework.module.modules.command_old.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command_old.CommandModule;
import de.timmi6790.discord_framework.module.modules.command_old.CommandParameters;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class MessageListenerTest {
    private static final long TEST_BOT_ID = 300000;
    private static final String[] testArgs = new String[]{"sadsdasdasadsads sad as das asda sad dsa", "\n\nHey"};

    private void runParsedStartTest(final Pattern mainPattern, final String mainMessage) {
        for (final String testArg : testArgs) {
            final String expectedArg = testArg.replace("\n", "");
            final Optional<String> resultNoSpace = MessageListener.getParsedStart(
                    mainMessage + testArg,
                    mainPattern
            );
            assertThat(resultNoSpace)
                    .isPresent()
                    .hasValue(expectedArg);

            final Optional<String> resultSpace = MessageListener.getParsedStart(
                    mainMessage + " " + testArg,
                    mainPattern
            );
            assertThat(resultSpace)
                    .isPresent()
                    .hasValue(expectedArg);
        }
    }

    private MessageListener createMessageListener(final long botId,
                                                  final String mainCommand,
                                                  final AbstractCommand helpCommand) {
        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getBotId()).thenReturn(botId);
        when(commandModule.getMainCommandPattern()).thenReturn(CommandModule.compileMainCommandPattern(mainCommand, botId));

        final GuildDb guildDb = mock(GuildDb.class);

        final GuildDbModule guildDbModule = mock(GuildDbModule.class);
        when(guildDbModule.getOrCreate(0)).thenReturn(guildDb);

        return new MessageListener(commandModule, guildDbModule, helpCommand);
    }

    private MessageReceivedEvent createMessageReceivedEvent(final long userId,
                                                            final String messageRaw,
                                                            final boolean fromGuild) {
        final MessageReceivedEvent messageReceivedEvent = mock(MessageReceivedEvent.class);
        final User user = mock(User.class);
        when(user.getIdLong()).thenReturn(userId);
        when(messageReceivedEvent.getAuthor()).thenReturn(user);

        final Message message = mock(Message.class);
        when(message.isFromGuild()).thenReturn(false);
        when(message.getContentRaw()).thenReturn(messageRaw);
        when(messageReceivedEvent.getMessage()).thenReturn(message);

        return messageReceivedEvent;
    }

    private void runOnTextMessageTest(final String mainCommand,
                                      final String rawMessage,
                                      final boolean fromGuild,
                                      final VerificationMode verificationMode) {
        final AbstractCommand helpCommand = mock(AbstractCommand.class);
        try (final MockedStatic<CommandParameters> commandParametersMockedStatic = mockStatic(CommandParameters.class)) {
            final CommandParameters commandParameters = mock(CommandParameters.class);
            commandParametersMockedStatic.when(() -> CommandParameters.of(any(), anyString())).thenReturn(commandParameters);

            this.createMessageListener(1, mainCommand, helpCommand)
                    .onTextMessage(this.createMessageReceivedEvent(2, rawMessage, fromGuild));
            Mockito.verify(helpCommand, verificationMode).runCommand(any());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"stat", "!", "<@!" + TEST_BOT_ID + ">"})
    void getParsedStartMainCommands(final String mainCommand) {
        final Pattern mainPattern = CommandModule.compileMainCommandPattern(mainCommand, TEST_BOT_ID);
        this.runParsedStartTest(mainPattern, mainCommand);
    }

    @ParameterizedTest
    @ValueSource(strings = {"<@!" + TEST_BOT_ID + ">", "<@&" + TEST_BOT_ID + ">"})
    void getParsedStartBotTag(final String botTag) {
        final Pattern mainPattern = CommandModule.compileMainCommandPattern("!", TEST_BOT_ID);
        this.runParsedStartTest(mainPattern, botTag);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onTextMessage_ignore_self(final boolean fromGuild) {
        final long userId = 1L;

        assertThatCode(() ->
                this.createMessageListener(userId, "!", mock(AbstractCommand.class))
                        .onTextMessage(this.createMessageReceivedEvent(userId, "", fromGuild))
        ).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"!", "stat "})
    void onTextMessage_help_message(final String mainCommand) {
        for (final boolean isGuild : new boolean[]{true, false}) {
            this.runOnTextMessageTest(mainCommand, mainCommand, isGuild, atLeastOnce());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"!", "stat "})
    void onTextMessage_incorrect_input(final String mainCommand) {
        for (final boolean isGuild : new boolean[]{true, false}) {
            this.runOnTextMessageTest(mainCommand, "", isGuild, never());
        }
    }
}