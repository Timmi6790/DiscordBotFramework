package de.timmi6790.discord_framework.modules.feedback.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.property.properties.AllowBotCommandProperty;
import de.timmi6790.discord_framework.modules.feedback.FeedbackHandler;
import de.timmi6790.discord_framework.modules.feedback.FeedbackModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FeedbackCommand extends AbstractCommand {
    private final FeedbackModule feedbackModule;

    public FeedbackCommand() {
        super("feedback", "Info", "Give your feedback", "<category>", "fb");

        this.addProperties(
                new AllowBotCommandProperty(false)
        );

        this.feedbackModule = getModuleManager().getModuleOrThrow(FeedbackModule.class);
    }

    private FeedbackHandler getFeedbackHandlerThrow(final CommandParameters commandParameters, final int argPos) {
        final String userInput = commandParameters.getArgs()[argPos];
        final Optional<FeedbackHandler> feedbackHandlerOpt = this.feedbackModule.getFeedbackHandler(userInput);
        if (feedbackHandlerOpt.isPresent()) {
            return feedbackHandlerOpt.get();
        }

        final List<FeedbackHandler> feedbackHandlers = DataUtilities.getSimilarityList(userInput, this.feedbackModule.getFeedbackHandlers(), FeedbackHandler::getFeedbackName, 0.6, 4);
        this.sendHelpMessage(
                commandParameters,
                userInput,
                argPos,
                "category",
                null,
                null,
                feedbackHandlers.stream().map(FeedbackHandler::getFeedbackName).collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        if (commandParameters.isGuildCommand()) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setDescription("This command can only be used in your pms with the bot!"),
                    90
            );

            return CommandResult.SUCCESS;
        }

        if (commandParameters.getArgs().length == 0) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setDescription("Available feedback categories.\n\n" +
                                    this.feedbackModule.getFeedbackHandlers()
                                            .stream()
                                            .map(FeedbackHandler::getFeedbackName)
                                            .sorted()
                                            .collect(Collectors.joining("\n"))
                            ),
                    300
            );
            return CommandResult.SUCCESS;
        }

        final FeedbackHandler feedbackHandler = this.getFeedbackHandlerThrow(commandParameters, 0);
        this.feedbackModule.getActiveFeedbackCache().put(commandParameters.getUserDb().getDiscordId(), feedbackHandler.getFeedbackName());
        feedbackHandler.onInitialize(commandParameters.getUser());

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Feedback Info")
                        .setDescription(feedbackHandler.getFeedbackInfoMessage())
                        .setFooter("Write cancel to cancel your feedback | You have " + FeedbackModule.getFEEDBACK_TIME() + " minutes to enter your feedback"),
                350
        );

        return CommandResult.SUCCESS;
    }
}
