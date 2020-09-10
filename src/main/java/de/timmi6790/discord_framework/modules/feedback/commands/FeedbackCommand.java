package de.timmi6790.discord_framework.modules.feedback.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.properties.AllowBotCommandProperty;
import de.timmi6790.discord_framework.modules.feedback.FeedbackHandler;
import de.timmi6790.discord_framework.modules.feedback.FeedbackModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FeedbackCommand extends AbstractCommand<FeedbackModule> {
    public FeedbackCommand() {
        super("feedback", "Info", "Give your feedback", "<category>", "fb");

        this.addProperties(
                new AllowBotCommandProperty(false)
        );
    }

    private FeedbackHandler getFeedbackHandlerThrow(final CommandParameters commandParameters, final int argPos) {
        final String userInput = commandParameters.getArgs()[argPos];
        final FeedbackModule module = this.getModule();

        final Optional<FeedbackHandler> feedbackHandlerOpt = module.getFeedbackHandler(userInput);
        if (feedbackHandlerOpt.isPresent()) {
            return feedbackHandlerOpt.get();
        }

        final List<FeedbackHandler> feedbackHandlers = DataUtilities.getSimilarityList(userInput, module.getFeedbackHandlers(), FeedbackHandler::getFeedbackName, 0.6, 4);
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
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setDescription("This command can only be used in your pms with the bot!"),
                    90
            );

            return CommandResult.SUCCESS;
        }

        if (commandParameters.getArgs().length == 0) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setDescription("Available feedback categories.\n\n" +
                                    this.getModule().getFeedbackHandlers()
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
        this.getModule().getActiveFeedbackCache().put(commandParameters.getUserDb().getDiscordId(), feedbackHandler.getFeedbackName());
        feedbackHandler.onInitialize(commandParameters.getUser());

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Feedback Info")
                        .setDescription(feedbackHandler.getFeedbackInfoMessage())
                        .setFooter("Write cancel to cancel your feedback | You have " + FeedbackModule.getFEEDBACK_TIME() + " minutes to enter your feedback"),
                350
        );

        return CommandResult.SUCCESS;
    }
}
