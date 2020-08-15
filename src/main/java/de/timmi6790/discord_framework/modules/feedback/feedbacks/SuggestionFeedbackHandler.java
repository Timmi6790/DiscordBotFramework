package de.timmi6790.discord_framework.modules.feedback.feedbacks;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.feedback.FeedbackHandler;
import net.dv8tion.jda.api.entities.User;

public class SuggestionFeedbackHandler implements FeedbackHandler {
    @Override
    public String getFeedbackName() {
        return "Suggestion";
    }

    @Override
    public String getFeedbackCategory() {
        return "Test";
    }

    @Override
    public String getFeedbackInfoMessage() {
        return "Suggestion";
    }

    @Override
    public void onInitialize(final User user) {

    }

    @Override
    public void onTextMessage(final CommandParameters commandParameters) {

    }
}