package de.timmi6790.discord_framework.modules.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandManagerTest {
    private static final long BOT_ID = 0;
    private static final String MAIN_COMMAND = "starto";

    private static final CommandManager COMMAND_MANAGER = new CommandManager(MAIN_COMMAND, BOT_ID);

    private CommandManager getCommandManager() {
        return new CommandManager(MAIN_COMMAND, BOT_ID);
    }

    private void commandPatternTester(final Pattern pattern, final String preValue, final String value, final String checkName) {
        final String invalidPattern = "%s command invalid pattern for \"%s\"";
        final String invalidGroup = "%s command invalid group for \"%s\" vs \"%s\"";

        final Matcher matcher = pattern.matcher(preValue + value);
        assertThat(matcher.find())
                .withFailMessage(invalidPattern, checkName, value)
                .isTrue();
        assertThat(matcher.group(1))
                .withFailMessage(invalidGroup, checkName, matcher.group(1), value)
                .isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", " test", "test", "test dsad s asdas sa asddasads "})
    void correctMainCommandPattern(final String valueAfterStart) {
        final Pattern pattern = COMMAND_MANAGER.getMainCommandPattern();
        // Main command
        this.commandPatternTester(pattern, MAIN_COMMAND, valueAfterStart, "Main");

        // bot tag command
        this.commandPatternTester(pattern, "<@!" + BOT_ID + ">", valueAfterStart, "Tag");

        // Nick bot tag command
        this.commandPatternTester(pattern, "<@&" + BOT_ID + ">", valueAfterStart, "Nick Tag");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", " test", "test", "test dsad s asdas sa asddasads "})
    void incorrectMainCommandPattern(final String valueAfterStart) {
        if (valueAfterStart.startsWith(MAIN_COMMAND)) {
            return;
        }

        final Pattern pattern = COMMAND_MANAGER.getMainCommandPattern();
        assertThat(valueAfterStart).doesNotContainPattern(pattern);
    }

    @Test
    void registerCommand() {
        final CommandManager commandManager = this.getCommandManager();
        final AbstractCommand command = new ExampleCommand();
        assertThat(commandManager.registerCommand(command)).isTrue();
    }

    @Test
    void getCommandByClass() {
        final CommandManager commandManager = this.getCommandManager();
        final AbstractCommand command = new ExampleCommand();
        commandManager.registerCommands(command);

        final AbstractCommand foundCommand = commandManager.getCommand(command.getClass()).orElse(null);
        assertThat(command).isEqualTo(foundCommand);
    }

    @Test
    void getCommandByName() {
        final CommandManager commandManager = this.getCommandManager();
        final AbstractCommand command = new ExampleCommand();
        commandManager.registerCommands(command);

        final AbstractCommand foundCommand = commandManager.getCommand(command.getName()).orElse(null);
        assertThat(command).isEqualTo(foundCommand);
    }
}
