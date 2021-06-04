package de.timmi6790.discord_framework.module.modules.reactions.emote.actions;

import lombok.Data;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

@Data
public class EmptyEmoteAction implements EmoteAction {
    @Override
    public void onEmote(final MessageReactionAddEvent reactionAddEvent) {
        // Doing nothing is my entire purpose
    }
}
