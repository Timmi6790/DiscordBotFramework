package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class CommandModuleTest {
    @SneakyThrows
    private CommandModule getCommandModule() {
        return new CommandModule();
    }

    @SneakyThrows
    private <T extends AbstractCommand> T createCommand(final Class<?> commandClass, final CommandModule commandModule) {
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

            return (T) commandClass.getConstructor().newInstance();
        }
    }

    @Test
    void registerCommand() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = this.createCommand(TestCommand.class, commandModule);

        final boolean registered = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered).isTrue();
        assertThat(commandModule.getCommands()).containsExactly(testCommand);
    }

    @Test
    void registerCommands() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = this.createCommand(TestCommand.class, commandModule);
        final TestCommand2 testCommand2 = this.createCommand(TestCommand2.class, commandModule);

        commandModule.registerCommands(
                testModule,
                testCommand,
                testCommand2
        );
        assertThat(commandModule.getCommands()).containsExactlyInAnyOrder(testCommand, testCommand2);
    }

    @Test
    void getCommandClass() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = this.createCommand(TestCommand.class, commandModule);

        commandModule.registerCommands(testModule, testCommand);
        final Optional<AbstractCommand> commandFound = commandModule.getCommand(TestCommand.class);
        assertThat(commandFound)
                .isPresent()
                .hasValue(testCommand);
    }

    @Test
    void getCommandClassEmpty() {
        final CommandModule commandModule = this.getCommandModule();

        final Optional<AbstractCommand> commandFound = commandModule.getCommand(TestCommand.class);
        assertThat(commandFound)
                .isNotPresent();
    }

    @Test
    void getCommandName() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = this.createCommand(TestCommand.class, commandModule);

        commandModule.registerCommands(testModule, testCommand);
        final Optional<AbstractCommand> commandFound = commandModule.getCommand(testCommand.getName());
        assertThat(commandFound)
                .isPresent()
                .hasValue(testCommand);
    }

    @Test
    void getCommands() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();

        final List<AbstractCommand> addedCommands = new ArrayList<>();
        Collections.addAll(
                addedCommands,
                this.createCommand(TestCommand.class, commandModule),
                this.createCommand(TestCommand2.class, commandModule),
                this.createCommand(TestCommand3.class, commandModule),
                this.createCommand(TestCommand4.class, commandModule),
                this.createCommand(TestCommand5.class, commandModule),
                this.createCommand(TestCommand6.class, commandModule)
        );

        commandModule.registerCommands(
                testModule,
                addedCommands.toArray(new AbstractCommand[0])
        );

        assertThat(commandModule.getCommands()).containsExactlyInAnyOrderElementsOf(addedCommands);
    }

    @Test
    void registerDupClassCommand() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = this.createCommand(TestCommand.class, commandModule);

        final boolean registered = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered).isTrue();

        final boolean registered1 = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered1).isFalse();

        assertThat(commandModule.getCommands()).containsExactly(testCommand);
    }

    @Test
    void registerDupNameCommand() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand6 testCommand = this.createCommand(TestCommand6.class, commandModule);
        final TestCommand6Dub testCommand6Dub = this.createCommand(TestCommand6Dub.class, commandModule);

        final boolean registered = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered).isTrue();

        final boolean registered1 = commandModule.registerCommand(testModule, testCommand6Dub);
        assertThat(registered1).isFalse();

        assertThat(commandModule.getCommands()).containsExactly(testCommand);
    }

    private static class TestModule extends AbstractModule {
        public TestModule() {
            super("TestModule");
        }
    }

    private static class TestCommand extends AbstractCommand {
        public TestCommand(final String name) {
            super(name, "", "", "");

            this.setPermissionId(1);
            this.setDbId(1);
        }

        public TestCommand() {
            this("test");
        }

        @Override
        protected CommandResult onCommand(final CommandParameters commandParameters) {
            return null;
        }
    }

    private static class TestCommand2 extends TestCommand {
        public TestCommand2() {
            super("test2");
        }
    }

    private static class TestCommand3 extends TestCommand {
        public TestCommand3() {
            super("test3");
        }
    }

    private static class TestCommand4 extends TestCommand {
        public TestCommand4() {
            super("ReallyLongName");
        }
    }

    private static class TestCommand5 extends TestCommand {
        public TestCommand5() {
            super("ReallyLongName1111111");
        }
    }

    private static class TestCommand6 extends TestCommand {
        public TestCommand6() {
            super("ReallyLongName1111111222222");
        }
    }

    private static class TestCommand6Dub extends TestCommand {
        public TestCommand6Dub() {
            super("ReallyLongName1111111222222");
        }
    }
}