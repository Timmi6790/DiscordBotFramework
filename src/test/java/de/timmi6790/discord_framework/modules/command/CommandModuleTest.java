package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.modules.AbstractModule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CommandModuleTest {
    private CommandModule getCommandModule() {
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