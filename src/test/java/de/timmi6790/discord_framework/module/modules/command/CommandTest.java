package de.timmi6790.discord_framework.module.modules.command;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.MessageUtilities;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CommandTest {
    private CommandParameters createCommandParameters(final Integer... permissionIds) {
        final UserDb userDb = mock(UserDb.class);

        final Set<Integer> permissions = new HashSet<>(Arrays.asList(permissionIds));
        when(userDb.getAllPermissionIds()).thenReturn(permissions);

        return this.createCommandParameters(userDb);
    }

    private CommandParameters createCommandParameters(final UserDb userDb) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);

        return commandParameters;
    }

    private CommandParameters createFullExecuteCommandCommandParameters(final boolean isGuildCommand) {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.isBanned()).thenReturn(false);

        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.isBanned()).thenReturn(false);

        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDb.getGuildDb()).thenReturn(guildDb);
        when(channelDb.getDiscordId()).thenReturn(1L);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);
        when(commandParameters.getGuildDb()).thenReturn(guildDb);
        when(commandParameters.getChannelDb()).thenReturn(channelDb);
        when(commandParameters.getArgs()).thenReturn(new String[0]);
        when(commandParameters.isGuildCommand()).thenReturn(isGuildCommand);
        when(commandParameters.getDiscordPermissions()).thenReturn(EnumSet.allOf(Permission.class));

        final JDA jda = mock(JDA.class);
        when(commandParameters.getJda()).thenReturn(jda);

        return commandParameters;
    }

    private Command createFullExecuteCommandCommand(final CommandParameters commandParameters) {
        final Command command = this.createCommand();
        when(command.canExecute(commandParameters)).thenReturn(true);

        final EventModule eventModule = mock(EventModule.class);
        when(command.getEventModule()).thenReturn(eventModule);

        final CommandProperty<?> property = mock(CommandProperty.class);
        when(property.onPermissionCheck(any(), any())).thenReturn(true);
        when(property.onCommandExecution(any(), any())).thenReturn(true);
        command.addProperty(property);

        return command;
    }

    private Command createCommand() {
        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getEventModule()).thenReturn(mock(EventModule.class));

        return spy(new TestCommand(commandModule));
    }

    @Test
    void addProperty() {
        final StringProperty stringProperty = new StringProperty();
        final BooleanProperty booleanProperty = new BooleanProperty();

        final Command command = this.createCommand();
        command.addProperty(stringProperty);
        command.addProperty(booleanProperty);
        
        assertThat(command.getProperty(stringProperty.getClass())).isPresent();
        assertThat(command.getProperty(booleanProperty.getClass())).isPresent();
    }

    @Test
    void addProperties() {
        final StringProperty stringProperty = new StringProperty();
        final BooleanProperty booleanProperty = new BooleanProperty();

        final Command command = this.createCommand();
        command.addProperties(stringProperty, booleanProperty);

        final Set<CommandProperty<?>> properties = command.getProperties();
        assertThat(properties).containsOnly(stringProperty, booleanProperty);
    }

    @Test
    void canExecute() {
        final int permissionId = 10;
        final Command command = this.createCommand();
        command.setPermissionId(permissionId);

        final CommandParameters commandParameters = this.createCommandParameters(permissionId);

        final CommandProperty<?> trueProperty = mock(CommandProperty.class);
        when(trueProperty.onPermissionCheck(command, commandParameters)).thenReturn(true);
        command.addProperty(trueProperty);

        // We meed to use a different class here or otherwise we can't register this
        final StringProperty trueProperty2 = mock(StringProperty.class);
        when(trueProperty2.onPermissionCheck(command, commandParameters)).thenReturn(true);
        command.addProperty(trueProperty2);

        final boolean canExecute = command.canExecute(commandParameters);
        assertThat(canExecute).isTrue();
    }

    @Test
    void canExecute_allowed_permission_id() {
        final int permissionId = 10;
        final Command command = this.createCommand();
        command.setPermissionId(permissionId);

        final CommandParameters commandParameters = this.createCommandParameters(permissionId);

        final boolean canExecute = command.canExecute(commandParameters);
        assertThat(canExecute).isTrue();
    }

    @Test
    void canExecute_missing_permission() {
        final int permissionId = 10;
        final Command command = this.createCommand();

        final CommandParameters commandParameters = this.createCommandParameters(permissionId);

        final boolean cantExecute = command.canExecute(commandParameters);
        assertThat(cantExecute).isFalse();
    }

    @Test
    void canExecute_invalid_permission() {
        final int permissionId = 10;
        final Command command = this.createCommand();
        command.setPermissionId(permissionId + 1);

        final CommandParameters commandParameters = this.createCommandParameters(permissionId);

        final boolean cantExecute = command.canExecute(commandParameters);
        assertThat(cantExecute).isFalse();
    }

    @Test
    void canExecute_allowed_property() {
        final int permissionId = 10;

        final CommandParameters commandParameters = this.createCommandParameters(permissionId);

        final Command command = this.createCommand();
        command.setPermissionId(permissionId);

        final CommandProperty<?> property = mock(CommandProperty.class);
        when(property.onPermissionCheck(command, commandParameters)).thenReturn(true);
        command.addProperty(property);

        final boolean canExecute = command.canExecute(commandParameters);
        assertThat(canExecute).isTrue();
    }

    @Test
    void canExecute_disallowed_property() {
        final int permissionId = 10;

        final CommandParameters commandParameters = this.createCommandParameters(permissionId);

        final Command command = this.createCommand();
        command.setPermissionId(permissionId);

        final CommandProperty<?> property = mock(CommandProperty.class);
        when(property.onPermissionCheck(command, commandParameters)).thenReturn(false);
        command.addProperty(property);

        final boolean cantExecute = command.canExecute(commandParameters);
        assertThat(cantExecute).isFalse();
    }

    @Test
    void canExecute_property() {
        final int permissionId = 10;

        final CommandParameters commandParameters = this.createCommandParameters(permissionId);

        final Command command = this.createCommand();
        command.setPermissionId(permissionId);

        final CommandProperty<?> trueProperty = mock(CommandProperty.class);
        when(trueProperty.onPermissionCheck(command, commandParameters)).thenReturn(true);
        command.addProperty(trueProperty);

        // We meed to use a different class here or otherwise we can't register this
        final StringProperty falseProperty = mock(StringProperty.class);
        when(falseProperty.onPermissionCheck(command, commandParameters)).thenReturn(false);
        command.addProperty(falseProperty);

        final boolean cantExecute = command.canExecute(commandParameters);
        assertThat(cantExecute).isFalse();
    }

    @Test
    void executeCommand_guild() {
        final CommandParameters commandParameters = this.createFullExecuteCommandCommandParameters(true);
        final Command command = this.createFullExecuteCommandCommand(commandParameters);

        command.executeCommand(commandParameters);
        verify(command).onCommand(any());
    }

    @Test
    void executeCommand_private_message() {
        final CommandParameters commandParameters = this.createFullExecuteCommandCommandParameters(false);
        final Command command = this.createFullExecuteCommandCommand(commandParameters);

        command.executeCommand(commandParameters);
        verify(command).onCommand(any());
    }

    @Test
    void executeCommand_return_null() {
        final CommandParameters commandParameters = this.createFullExecuteCommandCommandParameters(false);
        final Command command = this.createFullExecuteCommandCommand(commandParameters);
        when(command.onCommand(any())).thenReturn(null);

        command.executeCommand(commandParameters);
        verify(command).onCommand(any());
    }

    @Test
    void executeCommand_command_return_exception() {
        final CommandParameters commandParameters = this.createFullExecuteCommandCommandParameters(false);
        final Command command = this.createFullExecuteCommandCommand(commandParameters);
        when(command.onCommand(any())).thenThrow(CommandReturnException.class);

        command.executeCommand(commandParameters);
        verify(command).onCommand(any());
    }

    @Test
    void executeCommand_exception() {
        final CommandParameters commandParameters = this.createFullExecuteCommandCommandParameters(false);
        final Command command = this.createFullExecuteCommandCommand(commandParameters);
        when(command.onCommand(any())).thenThrow(RuntimeException.class);

        command.executeCommand(commandParameters);
        verify(command).onCommand(any());
    }

    @Test
    void executeCommand_banned_user() {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.isBanned()).thenReturn(true);

        final CommandParameters commandParameters = this.createCommandParameters(userDb);

        final Command command = this.createCommand();
        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            command.executeCommand(commandParameters);
            messageMock.verify(() -> MessageUtilities.sendUserBanMessage(commandParameters));
        }
        verify(command, never()).onCommand(commandParameters);
    }

    @Test
    void executeCommand_banned_guild() {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.isBanned()).thenReturn(false);

        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.isBanned()).thenReturn(true);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);
        when(commandParameters.getGuildDb()).thenReturn(guildDb);

        final Command command = this.createCommand();
        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            command.executeCommand(commandParameters);
            messageMock.verify(() -> MessageUtilities.sendGuildBanMessage(commandParameters));
        }
        verify(command, never()).onCommand(commandParameters);
    }

    @Test
    void executeCommand_invalid_discord_perms() {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.isBanned()).thenReturn(false);

        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.isBanned()).thenReturn(false);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);
        when(commandParameters.getGuildDb()).thenReturn(guildDb);
        when(commandParameters.isGuildCommand()).thenReturn(true);

        final Command command = this.createCommand();
        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            command.executeCommand(commandParameters);
            messageMock.verify(() -> MessageUtilities.sendMissingDiscordPermissionMessage(eq(commandParameters), anySet()));
        }
        verify(command, never()).onCommand(commandParameters);
    }

    @Test
    void executeCommand_invalid_perms() {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.isBanned()).thenReturn(false);

        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.isBanned()).thenReturn(false);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);
        when(commandParameters.getGuildDb()).thenReturn(guildDb);
        when(commandParameters.isGuildCommand()).thenReturn(true);
        when(commandParameters.getDiscordPermissions()).thenReturn(EnumSet.allOf(Permission.class));

        final Command command = this.createCommand();
        when(command.canExecute(commandParameters)).thenReturn(false);

        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            command.executeCommand(commandParameters);
            messageMock.verify(() -> MessageUtilities.sendMissingPermissionsMessage(commandParameters));
        }
        verify(command, never()).onCommand(commandParameters);
    }

    @Test
    void executeCommand_invalid_property() {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.isBanned()).thenReturn(false);

        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.isBanned()).thenReturn(false);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);
        when(commandParameters.getGuildDb()).thenReturn(guildDb);
        when(commandParameters.isGuildCommand()).thenReturn(true);
        when(commandParameters.getDiscordPermissions()).thenReturn(EnumSet.allOf(Permission.class));

        final Command command = this.createCommand();
        when(command.canExecute(commandParameters)).thenReturn(true);

        final CommandProperty<?> property = mock(CommandProperty.class);
        when(property.onPermissionCheck(any(), any())).thenReturn(true);
        when(property.onCommandExecution(any(), any())).thenReturn(false);
        command.addProperty(property);

        command.executeCommand(commandParameters);
        verify(command, never()).onCommand(commandParameters);
    }

    @Test
    void getProperty() {
        final Command command = this.createCommand();

        final StringProperty stringProperty = new StringProperty();

        final Optional<StringProperty> notFound = command.getProperty(StringProperty.class);
        assertThat(notFound).isNotPresent();

        command.addProperty(stringProperty);
        final Optional<StringProperty> found = command.getProperty(StringProperty.class);
        assertThat(found).isPresent();
    }

    @Test
    void getPropertyValueOrDefault() {
        final Command command = this.createCommand();

        final StringProperty stringProperty = new StringProperty();

        final String defaultValue = "A";
        assertThat(stringProperty.getValue()).isNotEqualTo(defaultValue);

        final String foundDefault = command.getPropertyValueOrDefault(StringProperty.class, () -> defaultValue);
        assertThat(foundDefault).isEqualTo(defaultValue);

        command.addProperty(stringProperty);
        final String foundProperty = command.getPropertyValueOrDefault(StringProperty.class, () -> defaultValue);
        assertThat(foundProperty).isEqualTo(stringProperty.getValue());
    }

    @Test
    void checkArgLength_valid_length() {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArgs()).thenReturn(new String[1]);

        final Command command = this.createCommand();

        assertDoesNotThrow(() -> command.checkArgLength(commandParameters, 1));
    }

    @Test
    void checkArgLength_invalid_length() {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArgs()).thenReturn(new String[0]);

        final Command command = this.createCommand();

        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            assertThrows(
                    CommandReturnException.class,
                    () -> command.checkArgLength(commandParameters, 1)
            );
        }
    }

    @Test
    void hasDefaultPermission() {
        final Command command = this.createCommand();

        final boolean hasDefault = command.hasDefaultPermission();
        assertThat(hasDefault).isTrue();
        command.setPermissionId(100);

        final boolean notDefault = command.hasDefaultPermission();
        assertThat(notDefault).isFalse();
    }

    private static class TestCommand extends Command {
        protected TestCommand(final CommandModule commandModule) {
            super("test", commandModule);
        }

        @Override
        protected CommandResult onCommand(final CommandParameters commandParameters) {
            return BaseCommandResult.SUCCESSFUL;
        }
    }

    private static class StringProperty implements CommandProperty<String> {
        @Override
        public String getValue() {
            return "Test";
        }
    }

    private static class BooleanProperty implements CommandProperty<Boolean> {
        @Override
        public Boolean getValue() {
            return Boolean.TRUE;
        }
    }
}