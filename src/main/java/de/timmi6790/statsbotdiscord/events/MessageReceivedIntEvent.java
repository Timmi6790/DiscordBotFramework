package de.timmi6790.statsbotdiscord.events;

import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MessageReceivedIntEvent extends net.dv8tion.jda.api.events.message.MessageReceivedEvent {

    public MessageReceivedIntEvent(final long responseNumber, @Nonnull final Message message) {
        super(StatsBot.getDiscord(), responseNumber, message);
    }

    public Optional<Member> getMemberOptional() {
        return Optional.ofNullable(this.getMember());
    }
}
