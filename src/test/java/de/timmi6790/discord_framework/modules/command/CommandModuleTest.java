package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.modules.AbstractModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class CommandModuleTest {
    @SneakyThrows
    private CommandModule getCommandModule() {
        // TODO: FIX ME
        /*
        final CommandModule commandModule = Mockito.spy(new CommandModule());

        final ModuleManager moduleManager = Mockito.spy(new ModuleManager());
        final ConfigModule configModule = Mockito.spy(new ConfigModule());
        final EventModule eventModule = Mockito.spy(new EventModule());
        final PermissionsModule permissionsModule = Mockito.spy(new PermissionsModule());

        doReturn(Optional.of(eventModule)).when(moduleManager).getModule(EventModule.class);
        doReturn(Optional.of(AbstractIntegrationTest.databaseModule)).when(moduleManager).getModule(DatabaseModule.class);
        doReturn(Optional.of(configModule)).when(moduleManager).getModule(ConfigModule.class);
        doReturn(Optional.of(permissionsModule)).when(moduleManager).getModule(PermissionsModule.class);

        doReturn(moduleManager).when(commandModule).getModuleManager();
        doReturn(moduleManager).when(permissionsModule).getModuleManager();

        final Config config = new Config();
        config.setMainCommand("stat ");
        doReturn(config).when(configModule).registerAndGetConfig(commandModule, new Config());

        final JDA jda = Mockito.spy(JDA.class);
        doReturn(jda).when(commandModule).getDiscord();

        final SelfUser selfUser = Mockito.spy(SelfUser.class);
        doReturn(selfUser).when(jda).getSelfUser();
        when(selfUser.getIdLong()).thenReturn(1L);

        permissionsModule.onInitialize();
        commandModule.onInitialize();

         */

        return new CommandModule();
    }

    @Test
    void registerCommand() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = new TestCommand();

        final boolean registered = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered).isTrue();
        assertThat(commandModule.getCommands())
                .hasSize(1)
                .contains(testCommand);
    }

    @Test
    void registerCommands() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = new TestCommand();
        final TestCommand2 testCommand2 = new TestCommand2();

        commandModule.registerCommands(
                testModule,
                testCommand,
                testCommand2
        );
        assertThat(commandModule.getCommands())
                .hasSize(2)
                .contains(testCommand)
                .contains(testCommand2);
    }

    @Test
    void getCommandClass() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = new TestCommand();

        commandModule.registerCommands(testModule, testCommand);
        final Optional<AbstractCommand<?>> commandFound = commandModule.getCommand(TestCommand.class);
        assertThat(commandFound)
                .isPresent()
                .hasValue(testCommand);
    }

    @Test
    void getCommandClassEmpty() {
        final CommandModule commandModule = this.getCommandModule();

        final Optional<AbstractCommand<?>> commandFound = commandModule.getCommand(TestCommand.class);
        assertThat(commandFound)
                .isNotPresent();
    }

    @Test
    void getCommandName() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = new TestCommand();

        commandModule.registerCommands(testModule, testCommand);
        final Optional<AbstractCommand<?>> commandFound = commandModule.getCommand(testCommand.getName());
        assertThat(commandFound)
                .isPresent()
                .hasValue(testCommand);
    }

    @Test
    void getCommands() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();

        final List<AbstractCommand<?>> addedCommands = new ArrayList<>();
        Collections.addAll(
                addedCommands,
                new TestCommand(),
                new TestCommand2(),
                new TestCommand3(),
                new TestCommand4(),
                new TestCommand5(),
                new TestCommand6()
        );

        commandModule.registerCommands(
                testModule,
                addedCommands.toArray(new AbstractCommand[0])
        );

        assertThat(commandModule.getCommands())
                .hasSize(addedCommands.size())
                .containsAll(addedCommands);
    }

    @Test
    void registerDupClassCommand() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand testCommand = new TestCommand();

        final boolean registered = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered).isTrue();

        final boolean registered1 = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered1).isFalse();

        assertThat(commandModule.getCommands())
                .hasSize(1)
                .contains(testCommand);
    }

    @Test
    void registerDupNameCommand() {
        final CommandModule commandModule = this.getCommandModule();
        final TestModule testModule = new TestModule();
        final TestCommand6 testCommand = new TestCommand6();
        final TestCommand6Dub testCommand6Dub = new TestCommand6Dub();

        final boolean registered = commandModule.registerCommand(testModule, testCommand);
        assertThat(registered).isTrue();

        final boolean registered1 = commandModule.registerCommand(testModule, testCommand6Dub);
        assertThat(registered1).isFalse();

        assertThat(commandModule.getCommands())
                .hasSize(1)
                .contains(testCommand);
    }

    @Test
    void compileMainCommandPattern() {
    }

    @Test
    void onInitialize() {
    }

    @Test
    void onEnable() {
    }

    @Test
    void innitDatabase() {
    }

    @Test
    void testRegisterCommands() {
    }

    @Test
    void testRegisterCommand() {
    }

    @Test
    void getCommand() {
    }

    @Test
    void testGetCommand() {
    }

    @Test
    void getSimilarCommands() {
    }

    @Test
    void testGetCommands() {
    }


    private static class TestModule extends AbstractModule {
        public TestModule() {
            super("TestModule");
        }
    }

    private static class TestCommand extends AbstractCommand<TestModule> {
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