package de.timmi6790.discord_framework.module.modules.command.listeners;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class MessageListenerTest {
    private MessageListener createMessageListener(final String mainCommand, final long botId) {
        final Command helpCommand = mock(Command.class);
        return this.createMessageListener(mainCommand, botId, helpCommand);
    }

    private MessageListener createMessageListener(final String mainCommand, final long botId, final Command helpCommand) {
        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getMainCommand()).thenReturn(mainCommand);
        when(commandModule.getBotId()).thenReturn(botId);

        final ChannelDbModule channelDbModule = mock(ChannelDbModule.class);
        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDbModule.getOrCreate(anyLong(), anyLong())).thenReturn(channelDb);

        final UserDbModule userDbModule = mock(UserDbModule.class);
        final UserDb userDb = mock(UserDb.class);
        when(userDbModule.getOrCreate(anyLong())).thenReturn(userDb);

        final ButtonReactionModule buttonReactionModule = mock(ButtonReactionModule.class);

        return new MessageListener(
                commandModule,
                userDbModule,
                channelDbModule,
                buttonReactionModule,
                helpCommand
        );
    }

    private MessageListener createMessageListener(final String mainCommand,
                                                  final long botId,
                                                  final Command helpCommand,
                                                  final String commandName,
                                                  final Command returnCommand) {
        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getMainCommand()).thenReturn(mainCommand);
        when(commandModule.getBotId()).thenReturn(botId);
        when(commandModule.getCommand(commandName)).thenReturn(Optional.of(returnCommand));

        final ChannelDbModule channelDbModule = mock(ChannelDbModule.class);
        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDbModule.getOrCreate(anyLong(), anyLong())).thenReturn(channelDb);

        final UserDbModule userDbModule = mock(UserDbModule.class);
        final UserDb userDb = mock(UserDb.class);
        when(userDbModule.getOrCreate(anyLong())).thenReturn(userDb);

        final ButtonReactionModule buttonReactionModule = mock(ButtonReactionModule.class);

        return new MessageListener(
                commandModule,
                userDbModule,
                channelDbModule,
                buttonReactionModule,
                helpCommand
        );
    }

    private MessageReceivedEvent createMessageEvent(final long userId, final String message) {
        final Message messageMock = mock(Message.class);
        when(messageMock.getContentRaw()).thenReturn(message);

        final User user = mock(User.class);
        when(user.getIdLong()).thenReturn(userId);

        final MessageChannel channel = mock(MessageChannel.class);
        when(channel.getIdLong()).thenReturn(1L);

        final Guild guild = mock(Guild.class);
        when(guild.getIdLong()).thenReturn(1L);

        final MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        when(event.getAuthor()).thenReturn(user);
        when(event.getMessage()).thenReturn(messageMock);
        when(event.getChannel()).thenReturn(channel);
        when(event.getGuild()).thenReturn(guild);

        return event;
    }

    private String getBotTag(final long botId, final boolean nicked) {
        if (nicked) {
            return "<@!" + botId + ">";
        }
        return "<@&" + botId + ">";
    }

    private void onTextMessage_only_bot_tag(final long botId, final boolean nicked) {
        final Command helpCommand = mock(Command.class);

        // The listener itself will remove any space from the main command
        final MessageListener messageListener = this.createMessageListener(
                "command",
                botId,
                helpCommand
        );

        final String message = this.getBotTag(botId, nicked);
        final MessageReceivedEvent event = this.createMessageEvent(botId + 1, message);

        messageListener.onTextMessage(event);
        verify(helpCommand).executeCommand(any());
    }

    @ParameterizedTest
    @ValueSource(longs = {9000L, 90000, 212132123132L})
    void onTextMessage_only_bot_tag(final long botId) {
        this.onTextMessage_only_bot_tag(botId, false);
        this.onTextMessage_only_bot_tag(botId, true);
    }


    @ParameterizedTest
    @ValueSource(strings = {"test", "test ", "test\n", "test \n \n \n", "test                     \n\t"})
    void onTextMessage_only_main_command(final String mainCommand) {
        final long botId = 1L;
        final Command helpCommand = mock(Command.class);

        // The listener itself will remove any space from the main command
        final MessageListener messageListener = this.createMessageListener(
                mainCommand.replace("\n", ""),
                botId,
                helpCommand
        );
        final MessageReceivedEvent event = this.createMessageEvent(botId + 1, mainCommand);

        messageListener.onTextMessage(event);
        verify(helpCommand).executeCommand(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "test ", "test\n", "1", "1 ", "1\n"})
    void onTextMessage_invalid_only_main_command(final String mainCommand) {
        final long botId = 1L;
        final Command helpCommand = mock(Command.class);

        // The listener itself will remove any space from the main command
        final MessageListener messageListener = this.createMessageListener(
                "command",
                botId,
                helpCommand
        );
        final MessageReceivedEvent event = this.createMessageEvent(botId + 1, mainCommand);

        messageListener.onTextMessage(event);
        verify(helpCommand, never()).executeCommand(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "test ", "test\n", "test \n \n \n", "test                     \n\t"})
    void onTextMessage_existing_command(final String mainCommand) {
        final long botId = 1L;
        final Command helpCommand = mock(Command.class);
        final Command returnCommand = mock(Command.class);

        // The listener itself will remove any space from the main command
        final String commandName = "command";
        final MessageListener messageListener = this.createMessageListener(
                mainCommand.replace("\n", ""),
                botId,
                helpCommand,
                commandName,
                returnCommand
        );
        final MessageReceivedEvent event = this.createMessageEvent(botId + 1, mainCommand + commandName);

        messageListener.onTextMessage(event);
        verify(returnCommand).executeCommand(any());
    }

    // TODO: Add the test for the help section after the help section is done

    @Test
    void onTextMessage_self_bot() {
        final long botId = 1L;
        final MessageListener messageListener = this.createMessageListener("", botId);

        final User user = mock(User.class);
        when(user.getIdLong()).thenReturn(botId);

        final MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        when(event.getAuthor()).thenReturn(user);

        // It would throw an exception when it gets past the self check, because the event contains no message
        assertDoesNotThrow(() -> messageListener.onTextMessage(event));
    }
}