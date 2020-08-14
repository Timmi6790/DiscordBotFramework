package de.timmi6790.discord_framework.modules.event.events;

import de.timmi6790.discord_framework.DiscordBot;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MessageReceivedIntEvent extends net.dv8tion.jda.api.events.message.MessageReceivedEvent {

    public MessageReceivedIntEvent(final long responseNumber, @Nonnull final Message message) {
        super(DiscordBot.getInstance().getDiscord(), responseNumber, message);
    }

    public Optional<Member> getMemberOptional() {
        return Optional.ofNullable(this.getMember());
    }
}
