package de.timmi6790.statsbotdiscord.modules.emotereaction.emotereactions;

import lombok.Data;

@Data
public class EmptyEmoteReaction implements AbstractEmoteReaction {
    @Override
    public void onEmote() {

    }
}
