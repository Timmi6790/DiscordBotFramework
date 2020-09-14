package de.timmi6790.discord_framework.modules.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageListenerTest {
    private static final long TEST_BOT_ID = 300000;
    private static final String[] testArgs = new String[]{"sadsdasdasadsads sad as das asda sad dsa", "\n\nHey"};
    
    private void runParsedStartTest(final Pattern mainPattern, final String mainMessage) {
        for (final String testArg : testArgs) {
            final String expectedArg = testArg.replace("\n", "");
            final Optional<String> resultNoSpace = MessageListener.getParsedStart(mainMessage + testArg, mainPattern, null);
            assertThat(resultNoSpace)
                    .isPresent()
                    .hasValue(expectedArg);

            final Optional<String> resultSpace = MessageListener.getParsedStart(mainMessage + " " + testArg, mainPattern, null);
            assertThat(resultSpace)
                    .isPresent()
                    .hasValue(expectedArg);
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
}