package de.timmi6790.discord_framework.modules.feedback;

import de.timmi6790.discord_framework.modules.GetModule;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
public class MessageListener extends GetModule<FeedbackModule> {
    private static final Pattern MESSAGE_SPLIT_PATTERN = Pattern.compile("\\s+");

    @SubscribeEvent
    public void onTextMessage(final MessageReceivedEvent event) {
        if (event.isFromGuild() || this.getModule().getActiveFeedbackCache().getIfPresent(event.getAuthor().getIdLong()) == null) {
            return;
        }

        final long userId = event.getAuthor().getIdLong();
        final String content = event.getMessage().getContentRaw();
        final CommandParameters commandParameters = new CommandParameters(
                content,
                MESSAGE_SPLIT_PATTERN.split(content),
                event.isFromGuild(),
                CommandCause.USER,
                this.getModuleManager().getModuleOrThrow(ChannelDbModule.class).getOrCreate(event.getChannel().getIdLong(), 0),
                this.getModuleManager().getModuleOrThrow(UserDbModule.class).getOrCreate(userId)
        );

        if (content.replace(" ", "").equalsIgnoreCase("cancel")) {
            this.getModule().getActiveFeedbackCache().invalidate(userId);
            commandParameters.getChannelDb().getChannel().sendMessage(
                    DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                            .setTitle("Canceled Feedback")
                            .setDescription("Your feedback input is now canceled.")
                            .build()
            ).queue();
            return;
        }

        this.getModule().getFeedbackHandler(this.getModule().getActiveFeedbackCache().getIfPresent(userId)).ifPresent(feedbackHandler -> feedbackHandler.onTextMessage(commandParameters));
    }
}
