package de.timmi6790.discord_framework.modules.emote_reaction.cache;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class CacheRemoveListener implements RemovalListener<Long, EmoteReactionMessage> {
    private final EmoteReactionModule module;

    @Override
    public void onRemoval(@Nullable final Long key, @Nullable final EmoteReactionMessage value, @NonNull final RemovalCause cause) {
        if (key == null || value == null) {
            return;
        }

        // Deduct the active emotes per player
        for (final long userId : value.getUsers()) {
            if (this.module.getActiveEmotesPerPlayer().containsKey(userId)) {
                final AtomicInteger currentCount = this.module.getActiveEmotesPerPlayer().get(userId);
                if (currentCount.get() > 1) {
                    currentCount.decrementAndGet();

                } else {
                    this.module.getActiveEmotesPerPlayer().remove(userId);
                }
            }
        }

        final MessageChannel channel = this.module.getDiscord().getTextChannelById(value.getChannelId());
        if (channel == null) {
            return;
        }

        channel.retrieveMessageById(key)
                .queue(message -> value.getEmotes()
                                .keySet()
                                .forEach(emote -> message.removeReaction(emote)
                                        .queue(null, new ErrorHandler().ignore(
                                                ErrorResponse.UNKNOWN_MESSAGE,
                                                ErrorResponse.MISSING_PERMISSIONS)
                                        )),
                        new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)
                );
    }
}
