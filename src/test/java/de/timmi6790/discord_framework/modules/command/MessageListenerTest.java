package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.datatypes.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class MessageListenerTest {
    private static final long TEST_BOT_ID = 300000;
    private static final String[] testArgs = new String[]{"sadsdasdasadsads sad as das asda sad dsa", "\n\nHey"};

    private void runParsedStartTest(final Pattern mainPattern, final String mainMessage) {
        for (final String testArg : testArgs) {
            final String expectedArg = testArg.replace("\n", "");
            final Optional<Pair<String, String>> resultNoSpace = MessageListener.getParsedStart(mainMessage + testArg, mainPattern, null);
            assertThat(resultNoSpace)
                    .isPresent()
                    .matches(stringStringPair -> {
                        final Pair<String, String> values = stringStringPair.get();
                        return values.getLeft().equalsIgnoreCase(mainMessage);
                    }, "Main command is not matching")
                    .matches(stringStringPair -> {
                        final Pair<String, String> values = stringStringPair.get();
                        return values.getRight().equalsIgnoreCase(expectedArg);
                    }, "Args are not matching");

            final Optional<Pair<String, String>> resultSpace = MessageListener.getParsedStart(mainMessage + " " + testArg, mainPattern, null);
            assertThat(resultSpace)
                    .isPresent()
                    .matches(stringStringPair -> {
                        final Pair<String, String> values = stringStringPair.get();
                        return values.getLeft().equalsIgnoreCase(mainMessage);
                    }, "Main command is not matching")
                    .matches(stringStringPair -> {
                        final Pair<String, String> values = stringStringPair.get();
                        return values.getRight().equalsIgnoreCase(expectedArg);
                    }, "Args are not matching");
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