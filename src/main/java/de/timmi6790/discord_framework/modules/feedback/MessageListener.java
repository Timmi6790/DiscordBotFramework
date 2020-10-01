package de.timmi6790.discord_framework.modules.feedback;

import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Pattern;

@AllArgsConstructor
public class MessageListener {
    private static final Pattern MESSAGE_SPLIT_PATTERN = Pattern.compile("\\s+");

    private final FeedbackModule feedbackModule;

    @SubscribeEvent
    public void onTextMessage(final MessageReceivedEvent event) {
        if (event.isFromGuild() || this.feedbackModule.getActiveFeedbackCache().getIfPresent(event.getAuthor().getIdLong()) == null) {
            return;
        }

        final long userId = event.getAuthor().getIdLong();
        final String content = event.getMessage().getContentRaw();
        final CommandParameters commandParameters = new CommandParameters(
                content,
                MESSAGE_SPLIT_PATTERN.split(content),
                event.isFromGuild(),
                CommandCause.USER,
                this.feedbackModule.getModuleOrThrow(ChannelDbModule.class).getOrCreate(event.getChannel().getIdLong(), 0),
                this.feedbackModule.getModuleOrThrow(UserDbModule.class).getOrCreate(userId)
        );

        if (content.replace(" ", "").equalsIgnoreCase("cancel")) {
            this.feedbackModule.getActiveFeedbackCache().invalidate(userId);
            DiscordMessagesUtilities.sendMessage(
                    event.getTextChannel(),
                    DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                            .setTitle("Canceled Feedback")
                            .setDescription("Your feedback input is now canceled.")
            );
            return;
        }

        this.feedbackModule.getFeedbackHandler(this.feedbackModule.getActiveFeedbackCache().getIfPresent(userId)).ifPresent(feedbackHandler -> feedbackHandler.onTextMessage(commandParameters));
    }
}
