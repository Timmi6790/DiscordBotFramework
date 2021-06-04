package de.timmi6790.discord_framework.module.modules.reactions.emote.emotereactions;

import lombok.Data;

@Data
public class EmptyEmoteReaction implements AbstractEmoteReaction {
    @Override
    public void onEmote() {
        // Doing nothing is my entire purpose
    }
}
