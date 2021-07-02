package de.timmi6790.discord_framework.module.modules.command_old;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.command_old.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command_old.property.CommandProperty;
import de.timmi6790.discord_framework.module.modules.command_old.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AbstractCommandTest {
    private void runInsideDiscordMessagesUtilitiesMock(final Runnable runnable) {
        try (final MockedStatic<DiscordMessagesUtilities> discordMessageMock = mockStatic(DiscordMessagesUtilities.class)) {
            final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
            discordMessageMock.when(() -> DiscordMessagesUtilities.getEmbedBuilder(any())).thenReturn(embedBuilder);

            runnable.run();
        }
    }

    private TestCommand createCommand() {
        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getMainCommand()).thenReturn("");
        return this.createCommand(commandModule);
    }

    private TestCommand createCommand(final CommandModule commandModule) {
        final ModuleManager moduleManager = mock(ModuleManager.class);

        final EventModule eventModule = mock(EventModule.class);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(eventModule);

        final AchievementModule achievementModule = new AchievementModule();

        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);
        doReturn(achievementModule).when(moduleManager).getModuleOrThrow(AchievementModule.class);

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);
            achievementModule.onInitialize();

            return spy(new TestCommand());
        }
    }

    private CommandParameters generateCommandParameters(final boolean fromGuild, final String... args) {
        final JDA jda = mock(JDA.class);

        final Guild guild = mock(Guild.class);
        // We only need it when from guild
        if (fromGuild) {
            final Member selfMember = mock(Member.class);
            when(selfMember.getPermissions(any())).thenReturn(AbstractCommand.MINIMUM_DISCORD_PERMISSIONS);
            when(guild.getSelfMember()).thenReturn(selfMember);

            when(guild.getJDA()).thenReturn(jda);
        }

        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.getDiscordId()).thenReturn(1L);
        when(guildDb.getGuild()).thenReturn(guild);

        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDb.getDiscordId()).thenReturn(1L);
        when(channelDb.getGuildDb()).thenReturn(guildDb);

        final UserDb userDb = mock(UserDb.class);
        when(userDb.getDiscordId()).thenReturn(1L);

        final CommandParameters commandParameters = spy(new CommandParameters(
                String.join(" ", args),
                args,
                fromGuild,
                CommandCause.USER,
                channelDb,
                userDb
        ));
        doReturn(jda).when(commandParameters).getJda();

        final TextChannel textChannel = mock(TextChannel.class);
        doReturn(textChannel).when(commandParameters).getLowestMessageChannel();

        return commandParameters;
    }

    @Test
    void hasRequiredDiscordPermsPrivateMessage() {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(false);

        assertThat(AbstractCommand.hasRequiredDiscordPerms(commandParameters, new HashSet<>())).isTrue();
    }

    @Test
    void hasRequiredDiscordPermsGuildMessageAllPermsFound() {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(true);

        final Set<Permission> extraPerms = EnumSet.of(Permission.NICKNAME_MANAGE);
        final Set<Permission> permissions = EnumSet.copyOf(AbstractCommand.MINIMUM_DISCORD_PERMISSIONS);
        permissions.addAll(extraPerms);
        when(commandParameters.getDiscordPermissions()).thenReturn(permissions);

        assertThat(AbstractCommand.hasRequiredDiscordPerms(commandParameters, extraPerms)).isTrue();
    }

    @Test
    void hasRequiredDiscordPermsGuildMessageMissingPerms() {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(true);
        when(commandParameters.getDiscordPermissions()).thenReturn(new HashSet<>());

        this.runInsideDiscordMessagesUtilitiesMock(
                () -> assertThat(AbstractCommand.hasRequiredDiscordPerms(commandParameters, new HashSet<>())).isFalse()
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isUserBanned(final boolean isBanned) {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.isBanned()).thenReturn(isBanned);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);

        final TestCommand testCommand = this.createCommand();
        this.runInsideDiscordMessagesUtilitiesMock(
                () -> assertThat(testCommand.isUserBanned(commandParameters)).isEqualTo(isBanned)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isServerBanned(final boolean isBanned) {
        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.isBanned()).thenReturn(isBanned);

        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDb.getGuildDb()).thenReturn(guildDb);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getChannelDb()).thenReturn(channelDb);

        final TestCommand testCommand = this.createCommand();
        this.runInsideDiscordMessagesUtilitiesMock(
                () -> assertThat(testCommand.isServerBanned(commandParameters)).isEqualTo(isBanned)
        );
    }

    @Test
    void throwInvalidArg() {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArgs()).thenReturn(new String[]{"A"});

        final TestCommand testCommand = this.createCommand();
        this.runInsideDiscordMessagesUtilitiesMock(
                () -> assertThrows(
                        CommandReturnException.class,
                        () -> testCommand.throwInvalidArg(commandParameters, 0, "A")
                )
        );
    }

    @Test
    void hasPermissionNoIdSet() {
        final CommandParameters commandParameters = mock(CommandParameters.class);

        final TestCommand command = Mockito.spy(this.createCommand());
        when(command.getPermissionId()).thenReturn(-1);

        assertThat(command.hasPermission(commandParameters)).isTrue();
    }

    @Test
    void hasPermissionTrue() {
        final int permissionId = 10;

        final UserDb userDb = mock(UserDb.class);
        final Set<Integer> permissionIds = new HashSet<>();
        permissionIds.add(permissionId);
        when(userDb.getAllPermissionIds()).thenReturn(permissionIds);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);

        final TestCommand command = Mockito.spy(this.createCommand());
        when(command.getPermissionId()).thenReturn(permissionId);

        assertThat(command.hasPermission(commandParameters)).isTrue();
    }

    @Test
    void hasPermissionFalse() {
        final UserDb userDb = mock(UserDb.class);
        when(userDb.getAllPermissionIds()).thenReturn(new HashSet<>());

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);

        final TestCommand command = Mockito.spy(this.createCommand());
        when(command.getPermissionId()).thenReturn(10);

        assertThat(command.hasPermission(commandParameters)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void hasPermissionProperty(final boolean propertyStatus) {
        final int permissionId = 10;

        final UserDb userDb = mock(UserDb.class);
        final Set<Integer> permissionIds = new HashSet<>();
        permissionIds.add(permissionId);
        when(userDb.getAllPermissionIds()).thenReturn(permissionIds);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getUserDb()).thenReturn(userDb);

        final TestCommand command = Mockito.spy(this.createCommand());
        command.addProperty(new BooleanCommandProperty(propertyStatus));
        when(command.getPermissionId()).thenReturn(permissionId);

        assertThat(command.hasPermission(commandParameters)).isEqualTo(propertyStatus);
    }

    @Test
    void getPropertyValueOrDefaultNoPropertyFound() {
        final TestCommand command = Mockito.spy(this.createCommand());
        assertThat(command.getPropertyValueOrDefault(BooleanCommandProperty.class, true)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getPropertyValueOrDefaultPropertyFound(final boolean propertyStatus) {
        final TestCommand command = Mockito.spy(this.createCommand());
        command.addProperties(new BooleanCommandProperty(propertyStatus));
        assertThat(command.getPropertyValueOrDefault(BooleanCommandProperty.class, true)).isEqualTo(propertyStatus);
    }

    @Test
    void getFormattedExampleCommands() {
        final TestCommand testCommand = this.createCommand();
        testCommand.addProperty(new ExampleCommandsCommandProperty("1", "2"));
        assertThat(testCommand.getFormattedExampleCommands()).hasSize(2);
    }

    @Test
    void getFormattedExampleCommandsEmpty() {
        final TestCommand testCommand = this.createCommand();
        assertThat(testCommand.getFormattedExampleCommands()).isEmpty();
    }

    @Test
    void checkArgLengthInvalidLength() {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArgs()).thenReturn(new String[0]);

        final TestCommand command = this.createCommand();
        this.runInsideDiscordMessagesUtilitiesMock(() ->
                assertThrows(CommandReturnException.class, () -> command.checkArgLength(commandParameters, 1))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 100})
    void checkArgLength(final int length) {
        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.getArgs()).thenReturn(new String[length]);

        final TestCommand command = this.createCommand();
        assertThatCode(() -> command.checkArgLength(commandParameters, 2)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "a", "b", "100"})
    void getFromListIgnoreCaseThrowSuccess(final String value) {
        final CommandParameters commandParameters = this.generateCommandParameters(true, value.toLowerCase());

        final TestCommand command = this.createCommand();
        final List<String> values = Collections.singletonList(value.toUpperCase());
        assertThat(command.getFromListIgnoreCaseThrow(commandParameters, 0, values)).isEqualToIgnoringCase(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "a", "b", "100"})
    void getFromListIgnoreCaseThrowFail(final String value) {
        final CommandParameters commandParameters = this.generateCommandParameters(true, "");

        final TestCommand command = this.createCommand();
        final List<String> values = Collections.singletonList(value);
        this.runInsideDiscordMessagesUtilitiesMock(() ->
                assertThrows(CommandReturnException.class,
                        () -> command.getFromListIgnoreCaseThrow(
                                commandParameters,
                                0,
                                values
                        )
                )
        );
    }

    @Test
    void getFromEnumIgnoreCaseThrowFail() {
        final CommandParameters commandParameters = this.generateCommandParameters(true, "c");

        final TestCommand command = this.createCommand();
        final TestEnum[] values = TestEnum.values();
        this.runInsideDiscordMessagesUtilitiesMock(() ->
                assertThrows(CommandReturnException.class,
                        () -> command.getFromEnumIgnoreCaseThrow(
                                commandParameters,
                                0,
                                values
                        )
                )
        );
    }

    @Test
    void getFromEnumIgnoreCaseThrowSuccess() {
        final CommandParameters commandParameters = this.generateCommandParameters(true, TestEnum.TEST.name());

        final TestCommand command = this.createCommand();
        assertThat(command.getFromEnumIgnoreCaseThrow(commandParameters, 0, TestEnum.values()))
                .isEqualTo(TestEnum.TEST);
    }

    @Test
    void getCommandThrowSuccess() {
        final TestCommand expectedCommand = this.createCommand();

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getCommand(anyString())).thenReturn(Optional.of(expectedCommand));

        final TestCommand command = this.createCommand(commandModule);
        final CommandParameters commandParameters = this.generateCommandParameters(true, command.getName());
        this.runInsideDiscordMessagesUtilitiesMock(() ->
                assertThat(command.getCommandThrow(commandParameters, 0)).isEqualTo(expectedCommand)
        );
    }

    @Test
    void getCommandThrowFail() {
        final TestCommand command = this.createCommand();
        final CommandParameters commandParameters = this.generateCommandParameters(true, command.getName());

        this.runInsideDiscordMessagesUtilitiesMock(() -> {
            final CommandModule commandModule = mock(CommandModule.class);
            when(commandModule.getCommand(anyString())).thenReturn(Optional.empty());

            assertThrows(CommandReturnException.class,
                    () -> command.getCommandThrow(commandParameters, 0)
            );
        });
    }

    @Test
    void getDiscordUserThrowFail() {
        final TestCommand command = this.createCommand();
        final CommandParameters commandParameters = this.generateCommandParameters(true, "a");

        this.runInsideDiscordMessagesUtilitiesMock(() ->
                assertThrows(CommandReturnException.class,
                        () -> command.getDiscordUserThrow(commandParameters, 0)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void runCommand(final boolean fromGuild) {
        // TODO: Implement me correctly
        final String fakeCommandName = "test";
        final CommandParameters commandParameters = this.generateCommandParameters(fromGuild, fakeCommandName);

        final CommandModule commandModule = mock(CommandModule.class);
        final Bucket bucket = Bucket4j.builder()
                .addLimit(Bandwidth.classic(90, Refill.intervally(90, Duration.ofMinutes(1))))
                .build();
        when(commandModule.resolveRateBucket(commandParameters.getUserDb().getDiscordId())).thenReturn(bucket);

        // Command with perms
        final TestCommand commandWithPerms = this.createCommand(commandModule);
        when(commandModule.getCommand(commandWithPerms.getName())).thenReturn(Optional.of(commandWithPerms));

        // Command without perms
        final String fakeCommandNameMissingPerms = "test1";
        final TestCommand missingPermsCommand = this.createCommand(commandModule);
        missingPermsCommand.setPermissionId(999);
        final CommandParameters missingPermsCommandParameters = this.generateCommandParameters(fromGuild, fakeCommandNameMissingPerms);
        when(commandModule.getCommand(missingPermsCommand.getName())).thenReturn(Optional.of(missingPermsCommand));

        this.runInsideDiscordMessagesUtilitiesMock(() -> {
            // Command with enough perms
            commandWithPerms.runCommand(commandParameters);
            verify(commandWithPerms).onCommand(commandParameters);

            // Command with missing perms
            missingPermsCommand.runCommand(missingPermsCommandParameters);
            verify(missingPermsCommand, never()).onCommand(missingPermsCommandParameters);
        });
    }

    private static class TestCommand extends AbstractCommand {
        public TestCommand() {
            super("Test", "", "", "");
        }

        @Override
        protected CommandResult onCommand(final CommandParameters commandParameters) {
            return null;
        }
    }

    @AllArgsConstructor
    private static class BooleanCommandProperty implements CommandProperty<Boolean> {
        private final boolean state;

        @Override
        public Boolean getValue() {
            return this.state;
        }

        @Override
        public boolean onPermissionCheck(@NonNull final @NotNull AbstractCommand command,
                                         @NonNull final @NotNull CommandParameters commandParameters) {
            return this.getValue();
        }

        @Override
        public boolean onCommandExecution(@NonNull final AbstractCommand command,
                                          @NonNull final CommandParameters commandParameters) {
            return this.getValue();
        }
    }

    private enum TestEnum {
        TEST,
        A,
        B
    }
}