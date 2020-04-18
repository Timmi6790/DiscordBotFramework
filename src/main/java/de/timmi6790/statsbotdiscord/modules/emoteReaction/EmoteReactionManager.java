package de.timmi6790.statsbotdiscord.modules.emoteReaction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.eventhandler.SubscribeEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;

public class EmoteReactionManager {
    private final static Cache<Long, EmoteReactionMessage> EMOTE_MESSAGE_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfter(new Expiry<Long, EmoteReactionMessage>() {

                @Override
                public long expireAfterCreate(@NonNull final Long key, @NonNull final EmoteReactionMessage value, final long currentTime) {
                    return TimeUnit.SECONDS.toNanos(value.getDeleteTime());
                }

                @Override
                public long expireAfterUpdate(@NonNull final Long key, @NonNull final EmoteReactionMessage value, final long currentTime, @NonNegative final long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(@NonNull final Long key, @NonNull final EmoteReactionMessage value, final long currentTime, @NonNegative final long currentDuration) {
                    return currentDuration;
                }
            })
            .removalListener((key, value, cause) -> {
                if (key == null || value == null) {
                    return;
                }

                final MessageChannel channel = StatsBot.getDiscord().getTextChannelById(value.getChannelId());
                if (channel == null) {
                    return;
                }


                channel.retrieveMessageById(key).queue((message) -> {
                    for (final String emote : value.getEmotes().keySet()) {
                        try {
                            message.removeReaction(emote).queue();
                        } catch (final Exception e) {
                            break;
                        }
                    }
                }, (failure) -> {
                });
            })
            .build();

    public EmoteReactionManager() {
        StatsBot.getEventManager().addEventListener(this);
    }

    public void addEmoteReactionMessage(final Message message, final EmoteReactionMessage emoteReactionMessage) {
        EMOTE_MESSAGE_CACHE.put(message.getIdLong(), emoteReactionMessage);

        for (final String emote : emoteReactionMessage.getEmotes().keySet()) {
            message.addReaction(emote).queue((m) -> {
            }, (failure) -> {
            });
        }
    }

    @SubscribeEvent
    public void onReactionAdd(final MessageReactionAddEvent event) {
        final EmoteReactionMessage emoteReactionMessage = EMOTE_MESSAGE_CACHE.getIfPresent(event.getMessageIdLong());
        if (emoteReactionMessage == null ||
                !emoteReactionMessage.getUsers().contains(event.getUserIdLong()) ||
                !emoteReactionMessage.getEmotes().containsKey(event.getReaction().getReactionEmote().getName())) {
            return;
        }

        if (emoteReactionMessage.isDeleteMessage()) {
            final MessageChannel channel = StatsBot.getDiscord().getTextChannelById(emoteReactionMessage.getChannelId());
            if (channel != null) {
                channel.deleteMessageById(event.getMessageIdLong()).queue();
            }

        } else if (emoteReactionMessage.isOneTimeUse()) {
            EMOTE_MESSAGE_CACHE.invalidate(event.getMessageIdLong());
        }

        emoteReactionMessage.getEmotes().get(event.getReaction().getReactionEmote().getName()).onEmote();
    }

    @SubscribeEvent
    public void onMessageDelete(final MessageDeleteEvent event) {
        EMOTE_MESSAGE_CACHE.invalidate(event.getMessageIdLong());
    }
}
