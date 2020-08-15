package de.timmi6790.discord_framework.modules.feedback.feedbacks;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.GetModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.feedback.FeedbackHandler;
import de.timmi6790.discord_framework.modules.feedback.FeedbackModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import net.dv8tion.jda.api.entities.User;

import java.util.UUID;

public class BugFeedbackHandler extends GetModule<FeedbackModule> implements FeedbackHandler {
    @Override
    public String getFeedbackName() {
        return "Bug";
    }

    @Override
    public String getFeedbackCategory() {
        return "Test";
    }

    @Override
    public String getFeedbackInfoMessage() {
        return "Please keep it limited to one message and be as detailed as possible, thx.";
    }

    @Override
    public void onInitialize(final User user) {

    }

    @Override
    public void onTextMessage(final CommandParameters commandParameters) {
        final UUID ticketId = UUID.randomUUID();

        final long channelId = this.getModule().getConfig().getFeedbackConfigs().get(this.getFeedbackName()).getChannelId();
        if (channelId != -1) {
            DiscordBot.getInstance().getDiscord()
                    .getTextChannelById(channelId)
                    .sendMessage(
                            DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                                    .setTitle(String.format("BugReport - %s", ticketId))
                                    .setDescription(commandParameters.getRawArgs())
                                    .setFooter(commandParameters.getUserDb().getDiscordId() + " | " + commandParameters.getUser().getAsTag())
                                    .build()
                    ).queue();
        }

        commandParameters.getTextChannel().sendMessage(
                DiscordMessagesUtilities
                        .getEmbedBuilder(commandParameters)
                        .setTitle("Bug Report")
                        .setDescription("Thx for submitting your bug report!")
                        .setFooter(ticketId.toString())
                        .build()
        ).queue();
        this.getModule().getActiveFeedbackCache().invalidate(commandParameters.getUserDb().getDiscordId());
    }
}
