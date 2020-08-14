package de.timmi6790.discord_framework.modules.feedback;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import net.dv8tion.jda.api.entities.User;

public interface FeedbackHandler {
    String getFeedbackName();

    String getFeedbackCategory();

    String getFeedbackInfoMessage();

    void onInitialize(User user);

    void onTextMessage(CommandParameters commandParameters);
}
