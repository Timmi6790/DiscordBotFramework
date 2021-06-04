package de.timmi6790.discord_framework.module.modules.reactions.emote.actions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface EmoteAction {
    void onEmote(MessageReactionAddEvent reactionAddEvent);
}
