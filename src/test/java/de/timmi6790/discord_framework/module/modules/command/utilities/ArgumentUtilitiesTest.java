package de.timmi6790.discord_framework.module.modules.command.utilities;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ArgumentUtilitiesTest {
    private CommandParameters createCommandParameters(final int argPosition, final String arg) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArg(argPosition)).thenReturn(arg);
        return commandParameters;
    }

    private void getDiscordUserOrThrow_success(final String input, final long userId) {
        final int argPosition = 0;

        final User expected = mock(User.class);

        final LoadingCache<Long, User> userCache = mock(LoadingCache.class);
        when(userCache.get(userId)).thenReturn(expected);

        final UserDbModule userDbModule = mock(UserDbModule.class);
        when(userDbModule.getDiscordUserCache()).thenReturn(userCache);

        final UserDb userDb = mock(UserDb.class);
        when(userDb.getUserDbModule()).thenReturn(userDbModule);

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, input);
        when(commandParameters.getUserDb()).thenReturn(userDb);

        final User result = ArgumentUtilities.getDiscordUserOrThrow(
                commandParameters,
                argPosition
        );
        assertThat(result).isEqualTo(expected);
    }

    private <T> void getNumberOrThrow(final T expected, final BiFunction<CommandParameters, Integer, T> function) {
        final int argPosition = 0;

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, String.valueOf(expected));
        final T result = function.apply(commandParameters, argPosition);
        assertThat(result).isEqualTo(expected);
    }

    private void getNumberOrThrow_exception(final BiFunction<CommandParameters, Integer, Number> function) {
        final int argPosition = 0;
        final String argument = "test";

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, argument);
        when(commandParameters.getEmbedBuilder()).thenReturn(new MultiEmbedBuilder());

        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            assertThrows(
                    CommandReturnException.class,
                    () -> function.apply(commandParameters, argPosition)
            );

            messageMock.verify(() ->
                    MessageUtilities.sendInvalidArgumentMessage(eq(commandParameters), eq(argument), anyString())
            );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "  ", "\n", "\t", "\n \t \n"})
    void parseRawArguments(final String delimiter) {
        final String[] expected = new String[]{"test", "test1", "test2", "test3", "test4", "test5"};
        final String input = String.join(delimiter, expected);
        final String[] result = ArgumentUtilities.parseRawArguments(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void parseRawArguments_empty_string() {
        final String[] result = ArgumentUtilities.parseRawArguments("");
        assertThat(result).isEmpty();
    }

    @Test
    void getFromEnumIgnoreCaseOrThrow_class() {
        final int argPosition = 0;
        final TestEnum expected = TestEnum.TEST3;

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, expected.name().toLowerCase());
        final TestEnum result = ArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                Command.class,
                argPosition,
                TestEnum.class
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFromEnumIgnoreCaseOrThrow_success() {
        final int argPosition = 0;
        final TestEnum expected = TestEnum.TEST1;

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, expected.name().toLowerCase());
        final TestEnum result = ArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                Command.class,
                argPosition,
                TestEnum.values()
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFromEnumIgnoreCaseOrThrow_exception() {
        final int argPosition = 0;

        final CommandModule commandModule = mock(CommandModule.class);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArg(argPosition)).thenReturn("D");
        when(commandParameters.getCommandModule()).thenReturn(commandModule);

        final TestEnum[] enumValues = TestEnum.values();
        assertThrows(
                CommandReturnException.class,
                () -> ArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                        commandParameters,
                        Command.class,
                        argPosition,
                        enumValues
                )
        );
    }

    @Test
    void getOrThrow_success() {
        final String expected = "Argument";
        final int argPosition = 0;

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, expected);
        final String result = ArgumentUtilities.getOrThrow(
                commandParameters,
                argPosition,
                Optional::of,
                "test"
        );
        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    void getOrThrow_exception() {
        final int argPosition = 0;
        final String argument = "test";

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, argument);
        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            assertThrows(
                    CommandReturnException.class,
                    () -> ArgumentUtilities.getOrThrow(
                            commandParameters,
                            argPosition,
                            arg -> Optional.empty(),
                            "test"
                    )
            );

            messageMock.verify(() ->
                    MessageUtilities.sendInvalidArgumentMessage(eq(commandParameters), eq(argument), anyString())
            );
        }
    }

    @Test
    void getDiscordUserOrThrow_success_number() {
        this.getDiscordUserOrThrow_success("305911488697204736", 305911488697204736L);
    }

    @Test
    void getDiscordUserOrThrow_success_tag() {
        this.getDiscordUserOrThrow_success("<@!305911488697204736>", 305911488697204736L);
    }

    @Test
    void getDiscordUserOrThrow_success_nicked_tag() {
        this.getDiscordUserOrThrow_success("<@&305911488697204736>", 305911488697204736L);
    }

    @Test
    void getDiscordUserOrThrow_exception_invalid_format() {
        final int argPosition = 0;

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, "Test");
        when(commandParameters.getEmbedBuilder()).thenReturn(new MultiEmbedBuilder());

        assertThrows(
                CommandReturnException.class,
                () -> ArgumentUtilities.getDiscordUserOrThrow(
                        commandParameters,
                        argPosition
                )
        );
        verify(commandParameters).sendMessage(any());
    }

    @Test
    void getDiscordUserOrThrow_exception_user_not_found() {
        final int argPosition = 0;

        final LoadingCache<Long, User> userCache = mock(LoadingCache.class);
        final UserDbModule userDbModule = mock(UserDbModule.class);
        when(userDbModule.getDiscordUserCache()).thenReturn(userCache);

        final UserDb userDb = mock(UserDb.class);
        when(userDb.getUserDbModule()).thenReturn(userDbModule);

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, "305911488697204736");
        when(commandParameters.getUserDb()).thenReturn(userDb);
        when(commandParameters.getEmbedBuilder()).thenReturn(new MultiEmbedBuilder());

        assertThrows(
                CommandReturnException.class,
                () -> ArgumentUtilities.getDiscordUserOrThrow(
                        commandParameters,
                        argPosition
                )
        );
        verify(commandParameters).sendMessage(any());
    }

    @Test
    void getPermissionIdOrThrow_success_command() {
        final String argument = "test";
        final int argPosition = 0;
        final int expected = 90;

        final Command command = mock(Command.class);
        when(command.getPermissionId()).thenReturn(expected);

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getCommand(argument)).thenReturn(Optional.of(command));

        final PermissionsModule permissionsModule = mock(PermissionsModule.class);

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, argument);

        final int result = ArgumentUtilities.getPermissionIdOrThrow(
                commandParameters,
                argPosition,
                commandModule,
                null,
                permissionsModule
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPermissionIdOrThrow_success_setting() {
        final String argument = "test";
        final int argPosition = 0;
        final int expected = 90;

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getCommand(argument)).thenReturn(Optional.empty());

        final AbstractSetting<?> setting = mock(AbstractSetting.class);
        when(setting.getPermissionId()).thenReturn(expected);

        final SettingModule settingsModule = mock(SettingModule.class);
        when(settingsModule.getSetting(argument)).thenReturn(Optional.of(setting));

        final PermissionsModule permissionsModule = mock(PermissionsModule.class);

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, argument);

        final int result = ArgumentUtilities.getPermissionIdOrThrow(
                commandParameters,
                argPosition,
                commandModule,
                settingsModule,
                permissionsModule
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPermissionIdOrThrow_success_permission() {
        final String arg = "test";
        final int argPosition = 0;
        final int expected = 90;

        final CommandModule commandModule = mock(CommandModule.class);
        final SettingModule settingsModule = mock(SettingModule.class);

        final PermissionsModule permissionsModule = mock(PermissionsModule.class);
        when(permissionsModule.getPermissionId(arg)).thenReturn(Optional.of(expected));

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, arg);

        final int result = ArgumentUtilities.getPermissionIdOrThrow(
                commandParameters,
                argPosition,
                commandModule,
                settingsModule,
                permissionsModule
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPermissionIdOrThrow_exception() {
        final String argument = "test";
        final int argPosition = 0;

        final CommandModule commandModule = mock(CommandModule.class);
        final SettingModule settingsModule = mock(SettingModule.class);
        final PermissionsModule permissionsModule = mock(PermissionsModule.class);

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, argument);
        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            assertThrows(
                    CommandReturnException.class,
                    () -> ArgumentUtilities.getPermissionIdOrThrow(
                            commandParameters,
                            argPosition,
                            commandModule,
                            settingsModule,
                            permissionsModule
                    )
            );

            messageMock.verify(() ->
                    MessageUtilities.sendInvalidArgumentMessage(eq(commandParameters), eq(argument), anyString())
            );
        }
    }

    @Test
    void getPermissionIdOrThrow_exception_command_default_permission() {
        final String argument = "test";
        final int argPosition = 0;

        final Command command = mock(Command.class);
        when(command.hasDefaultPermission()).thenReturn(true);
        when(command.getName()).thenReturn(argument);

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getCommand(argument)).thenReturn(Optional.of(command));

        final PermissionsModule permissionsModule = mock(PermissionsModule.class);

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, argument);

        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            assertThrows(
                    CommandReturnException.class,
                    () -> ArgumentUtilities.getPermissionIdOrThrow(
                            commandParameters,
                            argPosition,
                            commandModule,
                            null,
                            permissionsModule
                    )
            );

            messageMock.verify(() ->
                    MessageUtilities.sendErrorMessage(eq(commandParameters), anyString())
            );
        }
    }

    @Test
    void getRankOrThrow_success() {
        final String rankName = "rankTest";
        final int argPosition = 0;
        final Rank expected = mock(Rank.class);

        final RankModule rankModule = mock(RankModule.class);
        when(rankModule.getRank(rankName)).thenReturn(Optional.of(expected));

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, rankName);
        final Rank result = ArgumentUtilities.getRankOrThrow(commandParameters, argPosition, rankModule);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getRankOrThrow_exception() {
        final int argPosition = 0;
        final String argument = "test";

        final RankModule rankModule = mock(RankModule.class);
        when(rankModule.getRank(any())).thenReturn(Optional.empty());

        final CommandParameters commandParameters = this.createCommandParameters(argPosition, argument);
        try (final MockedStatic<MessageUtilities> messageMock = mockStatic(MessageUtilities.class)) {
            assertThrows(
                    CommandReturnException.class,
                    () -> ArgumentUtilities.getRankOrThrow(commandParameters, argPosition, rankModule)
            );

            messageMock.verify(() ->
                    MessageUtilities.sendInvalidArgumentMessage(eq(commandParameters), eq(argument), anyString())
            );
        }
    }

    @Test
    void getByteOrThrow_success() {
        this.getNumberOrThrow(Byte.MAX_VALUE, ArgumentUtilities::getByteOrThrow);
    }

    @Test
    void getByteOrThrow_exception() {
        this.getNumberOrThrow_exception(ArgumentUtilities::getByteOrThrow);
    }

    @Test
    void getShortOrThrow_success() {
        this.getNumberOrThrow(Short.MAX_VALUE, ArgumentUtilities::getShortOrThrow);
    }

    @Test
    void getShortOrThrow_exception() {
        this.getNumberOrThrow_exception(ArgumentUtilities::getShortOrThrow);
    }

    @Test
    void getIntOrThrow_success() {
        this.getNumberOrThrow(Integer.MAX_VALUE, ArgumentUtilities::getIntOrThrow);
    }

    @Test
    void getIntOrThrow_exception() {
        this.getNumberOrThrow_exception(ArgumentUtilities::getIntOrThrow);
    }

    @Test
    void getLongOrThrow_success() {
        this.getNumberOrThrow(Long.MAX_VALUE, ArgumentUtilities::getLongOrThrow);
    }

    @Test
    void getLongOrThrow_exception() {
        this.getNumberOrThrow_exception(ArgumentUtilities::getLongOrThrow);
    }

    @Test
    void getDoubleOrThrow_success() {
        this.getNumberOrThrow(Double.MAX_VALUE, ArgumentUtilities::getDoubleOrThrow);
    }

    @Test
    void getDoubleOrThrow_exception() {
        this.getNumberOrThrow_exception(ArgumentUtilities::getDoubleOrThrow);
    }

    @Test
    void getFloatOrThrow_success() {
        this.getNumberOrThrow(Float.MAX_VALUE, ArgumentUtilities::getFloatOrThrow);
    }

    @Test
    void getFloatOrThrow_exception() {
        this.getNumberOrThrow_exception(ArgumentUtilities::getFloatOrThrow);
    }

    private enum TestEnum {
        TEST1,
        TEST2,
        TEST3,
        DEFAULT1,
        DEFAULT2,
        DEFAULT3
    }
}