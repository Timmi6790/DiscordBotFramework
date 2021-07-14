package de.timmi6790.discord_framework.module.modules.command;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CommandModuleTest {
    private Command createCommand(final String name) {
        final Command command = mock(Command.class);
        return this.createCommand(command, name);
    }

    private Command createCommand(final String name, final String... aliasNames) {
        final Command command = mock(Command.class);
        return this.createCommand(command, name, aliasNames);
    }

    private Command createCommand(final Command command, final String name) {
        return this.createCommand(command, name, new String[0]);
    }

    private Command createCommand(final Command command, final String name, final String[] aliasNames) {
        when(command.getName()).thenReturn(name);
        when(command.getPropertyValueOrDefault(eq(AliasNamesProperty.class), any())).thenReturn(aliasNames);
        return command;
    }

    @Test
    void onInitialize() {
        final PermissionsModule permissionModule = mock(PermissionsModule.class);
        final ConfigModule configModule = mock(ConfigModule.class);
        final EventModule eventModule = mock(EventModule.class);

        final ModuleManager moduleManager = mock(ModuleManager.class);
        when(moduleManager.getModuleOrThrow(PermissionsModule.class)).thenReturn(permissionModule);
        when(moduleManager.getModuleOrThrow(ConfigModule.class)).thenReturn(configModule);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(eventModule);

        final CommandModule commandModule = new CommandModule();

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            final ShardManager discord = mock(ShardManager.class);
            when(bot.getDiscord()).thenReturn(discord);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            final boolean success = commandModule.onInitialize();
            assertThat(success).isTrue();
        }
    }

    @Test
    void onEnable() {
        final PermissionsModule permissionModule = mock(PermissionsModule.class);
        final EventModule eventModule = mock(EventModule.class);
        final UserDbModule userDbModule = mock(UserDbModule.class);
        final ChannelDbModule channelDbModule = mock(ChannelDbModule.class);

        final ConfigModule configModule = mock(ConfigModule.class);
        when(configModule.registerAndGetConfig(any(), any())).thenReturn(new Config());

        final ModuleManager moduleManager = mock(ModuleManager.class);
        when(moduleManager.getModuleOrThrow(PermissionsModule.class)).thenReturn(permissionModule);
        when(moduleManager.getModuleOrThrow(ConfigModule.class)).thenReturn(configModule);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(eventModule);
        when(moduleManager.getModuleOrThrow(UserDbModule.class)).thenReturn(userDbModule);
        when(moduleManager.getModuleOrThrow(ChannelDbModule.class)).thenReturn(channelDbModule);

        final CommandModule commandModule = new CommandModule();

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            final ShardManager discord = mock(ShardManager.class);
            when(bot.getDiscord()).thenReturn(discord);

            final JDA jda = mock(JDA.class);
            final SelfUser selfUser = mock(SelfUser.class);
            when(selfUser.getIdLong()).thenReturn(1L);
            when(jda.getSelfUser()).thenReturn(selfUser);
            when(bot.getBaseShard()).thenReturn(jda);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);

            commandModule.onInitialize();
            final boolean success = commandModule.onEnable();
            assertThat(success).isTrue();
        }
    }

    @Test
    void getCommand_name() {
        final CommandModule commandModule = new CommandModule();
        final Command command = this.createCommand("test");

        final Optional<Command> commandNotFound = commandModule.getCommand(command.getName());
        assertThat(commandNotFound).isEmpty();

        // Register command
        commandModule.registerCommand(commandModule, command);

        final Optional<Command> commandFound = commandModule.getCommand(command.getName());
        assertThat(commandFound).isPresent();
    }

    @Test
    void getCommand_alias_name() {
        final String aliasName = "aliasName";
        final CommandModule commandModule = new CommandModule();
        final Command command = this.createCommand("test", aliasName);

        final Optional<Command> commandNotFound = commandModule.getCommand(aliasName);
        assertThat(commandNotFound).isEmpty();

        // Register command
        commandModule.registerCommand(commandModule, command);

        final Optional<Command> commandFound = commandModule.getCommand(aliasName);
        assertThat(commandFound).isPresent();
    }

    @Test
    void getCommand_class() {
        final CommandModule commandModule = new CommandModule();

        // Register a fill command for the loop to iterate
        final Command commandFill = this.createCommand(mock(TestCommand.class), "test1");
        commandModule.registerCommand(commandModule, commandFill);

        final Command command = this.createCommand("test");

        final Optional<Command> commandNotFound = commandModule.getCommand(command.getClass());
        assertThat(commandNotFound).isEmpty();

        // Register command
        commandModule.registerCommand(commandModule, command);

        final Optional<Command> commandFound = commandModule.getCommand(command.getClass());
        assertThat(commandFound).isPresent();
    }

    @Test
    void getCommands_predicate() {
        final CommandModule commandModule = new CommandModule();

        final Map<String, Command> registeredCommands = Maps.newHashMapWithExpectedSize(5);
        for (int count = 0; 10 > count; count++) {
            final Command command = this.createCommand(String.valueOf(count));

            commandModule.registerCommand(commandModule, command);
            if (count % 2 == 0) {
                registeredCommands.put(command.getName(), command);
            }
        }

        final Set<Command> foundCommands = commandModule.getCommands(command -> registeredCommands.containsKey(command.getName()));
        assertThat(foundCommands).containsExactlyInAnyOrderElementsOf(registeredCommands.values());
    }

    @Test
    void registerCommands() {
        final CommandModule commandModule = new CommandModule();
        final Set<Command> registeredCommands = Sets.newHashSetWithExpectedSize(10);
        for (int count = 0; 10 > count; count++) {
            final Command command = this.createCommand(String.valueOf(count));
            registeredCommands.add(command);
        }
        commandModule.registerCommands(commandModule, registeredCommands.toArray(new Command[0]));


        final Set<Command> foundCommands = commandModule.getCommands();
        assertThat(foundCommands).containsExactlyInAnyOrderElementsOf(registeredCommands);
    }

    @Test
    void registerCommand() {
        final CommandModule commandModule = new CommandModule();
        final Command command = this.createCommand("test");

        final boolean success = commandModule.registerCommand(commandModule, command);
        assertThat(success).isTrue();
    }

    @Test
    void registerCommand_duplicate() {
        final CommandModule commandModule = new CommandModule();
        final Command command = this.createCommand("test");

        final boolean success = commandModule.registerCommand(commandModule, command);
        assertThat(success).isTrue();

        final boolean duplicate = commandModule.registerCommand(commandModule, command);
        assertThat(duplicate).isFalse();
    }

    @Test
    void registerCommand_changed_permission_id() {
        final int requiredPermissionId = 900;

        final PermissionsModule permissionModule = mock(PermissionsModule.class);
        when(permissionModule.addPermission(any())).thenReturn(requiredPermissionId);

        final CommandModule commandModule = spy(new CommandModule());
        when(commandModule.getPermissionsModule()).thenReturn(permissionModule);

        final Command command = this.createCommand("test");
        when(command.hasDefaultPermission()).thenReturn(true);

        final boolean success = commandModule.registerCommand(commandModule, command);
        assertThat(success).isTrue();

        verify(command).setPermissionId(requiredPermissionId);
    }

    @Test
    void registerCommand_alias_names() {
        final String aliasNameOne = "test1";
        final String aliasNameTwo = "test2";

        final CommandModule commandModule = new CommandModule();
        final Command command = this.createCommand("test", aliasNameOne, aliasNameTwo);

        commandModule.registerCommand(commandModule, command);

        final Optional<Command> foundAliasNameOne = commandModule.getCommand(aliasNameOne);
        assertThat(foundAliasNameOne).isPresent();

        final Optional<Command> foundAliasNameTwo = commandModule.getCommand(aliasNameTwo);
        assertThat(foundAliasNameTwo).isPresent();
    }

    @Test
    void registerCommand_alias_names_duplicate() {
        final String aliasName = "testAlias";

        final CommandModule commandModule = new CommandModule();

        final Command commandOne = this.createCommand("test", aliasName);
        final Command commandTwo = this.createCommand("test2", aliasName);

        commandModule.registerCommand(commandModule, commandOne);
        commandModule.registerCommand(commandModule, commandTwo);

        final Optional<Command> found = commandModule.getCommand(aliasName);
        assertThat(found)
                .isPresent()
                .contains(commandOne);
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
}