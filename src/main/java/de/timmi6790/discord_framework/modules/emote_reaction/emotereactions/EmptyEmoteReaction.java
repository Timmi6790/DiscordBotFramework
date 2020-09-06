package de.timmi6790.discord_framework.modules.emote_reaction.emotereactions;

import lombok.Data;

@Data
public class EmptyEmoteReaction implements AbstractEmoteReaction {
    @Override
    public void onEmote() {
        // Doing nothing is my entire purpose
    }
}
