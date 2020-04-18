package de.timmi6790.statsbotdiscord.modules.emoteReaction;

import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class EmoteReactionMessage {
    private final Map<String, AbstractEmoteReaction> emotes;
    private final List<Long> users;
    private final long channelId;
    private final int deleteTime;
    private final boolean oneTimeUse;
    private final boolean deleteMessage;

    public EmoteReactionMessage(final Map<String, AbstractEmoteReaction> emotes, final Long userId, final long channelId, final int deleteTime, final boolean oneTimeUse, final boolean deleteMessage) {
        this.emotes = emotes;
        this.users = Collections.singletonList(userId);
        this.channelId = channelId;
        this.deleteTime = deleteTime;
        this.oneTimeUse = oneTimeUse;
        this.deleteMessage = deleteMessage;
    }

    public EmoteReactionMessage(final Map<String, AbstractEmoteReaction> emotes, final Long userId, final long channelId, final int deleteTime) {
        this.emotes = emotes;
        this.users = Collections.singletonList(userId);
        this.channelId = channelId;
        this.deleteTime = deleteTime;
        this.oneTimeUse = true;
        this.deleteMessage = true;
    }

    public EmoteReactionMessage(final Map<String, AbstractEmoteReaction> emotes, final Long userId, final long channelId) {
        this.emotes = emotes;
        this.users = Collections.singletonList(userId);
        this.channelId = channelId;
        this.deleteTime = 300;
        this.oneTimeUse = true;
        this.deleteMessage = true;
    }
}
