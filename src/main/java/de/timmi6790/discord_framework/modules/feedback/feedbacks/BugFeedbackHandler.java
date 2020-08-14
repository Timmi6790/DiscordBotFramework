package de.timmi6790.discord_framework.modules.feedback.feedbacks;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.feedback.FeedbackHandler;
import net.dv8tion.jda.api.entities.User;

public class BugFeedbackHandler implements FeedbackHandler {
    @Override
    public String getFeedbackName() {
        return "bug";
    }

    @Override
    public String getFeedbackCategory() {
        return "Test";
    }

    @Override
    public String getFeedbackInfoMessage() {
        return "Bug";
    }

    @Override
    public void onInitialize(final User user) {
        System.out.println("innit");
    }

    @Override
    public void onTextMessage(final CommandParameters commandParameters) {
        System.out.println("message");
    }
}
