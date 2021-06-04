package de.timmi6790.discord_framework.module.modules.reactions.emote;

import de.timmi6790.discord_framework.module.modules.reactions.common.BaseReaction;
import de.timmi6790.discord_framework.module.modules.reactions.emote.actions.EmoteAction;
import lombok.*;

import java.util.Map;
import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class EmoteReaction extends BaseReaction {
    private final Map<String, EmoteAction> emotes;
    private final long channelId;

    public EmoteReaction(final Map<String, EmoteAction> emotes,
                         final Long userId,
                         final long channelId,
                         final int deleteTime) {
        this(emotes, userId, channelId, deleteTime, true, true);
    }

    public EmoteReaction(final Map<String, EmoteAction> emotes,
                         final Long userId,
                         final long channelId) {
        this(emotes, userId, channelId, 300, true, true);
    }

    public EmoteReaction(@NonNull final Map<String, EmoteAction> emotes,
                         final Long userId,
                         final long channelId,
                         final int deleteTime,
                         final boolean oneTimeUse,
                         final boolean deleteMessage) {
        super(Set.of(userId), deleteTime, oneTimeUse, deleteMessage);

        this.emotes = emotes;
        this.channelId = channelId;
    }
}
