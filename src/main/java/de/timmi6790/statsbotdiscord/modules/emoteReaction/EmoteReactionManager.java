package de.timmi6790.statsbotdiscord.modules.emoteReaction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.eventhandler.SubscribeEvent;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EmoteReactionManager {
    private static final int ACTIVE_EMOTES_LIMIT = 6;

    @Getter
    private final Map<Long, AtomicInteger> activeEmotesPerPlayer = new ConcurrentHashMap<>();

    @Getter
    private final Cache<Long, EmoteReactionMessage> emoteMessageCache = Caffeine.newBuilder()
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

                // Deduct the active emotes per player
                value.getUsers()
                        .stream()
                        .filter(this.activeEmotesPerPlayer::containsKey)
                        .forEach(user -> {
                            AtomicInteger currentCount = this.activeEmotesPerPlayer.get(user);
                            if (1 >= currentCount.get()) {
                                this.activeEmotesPerPlayer.remove(user);

                            } else {
                                currentCount.decrementAndGet();
                            }
                        });

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
        final Set<Long> playersAboveRate = new HashSet<>();
        for (final long user : emoteReactionMessage.getUsers()) {
            final AtomicInteger currentActive = this.activeEmotesPerPlayer.getOrDefault(user, new AtomicInteger(0));
            if (currentActive.getAndIncrement() >= ACTIVE_EMOTES_LIMIT) {
                playersAboveRate.add(user);
                continue;
            }

            this.activeEmotesPerPlayer.put(user, currentActive);
        }
        
        if (emoteReactionMessage.getUsers().size() == playersAboveRate.size()) {
            return;
        }
        emoteReactionMessage.getUsers().removeAll(playersAboveRate);

        this.emoteMessageCache.put(message.getIdLong(), emoteReactionMessage);

        for (final String emote : emoteReactionMessage.getEmotes().keySet()) {
            message.addReaction(emote).queue((m) -> {
            }, (failure) -> {
            });
        }
    }

    @SubscribeEvent
    public void onReactionAdd(final MessageReactionAddEvent event) {
        final EmoteReactionMessage emoteReactionMessage = this.emoteMessageCache.getIfPresent(event.getMessageIdLong());
        if (emoteReactionMessage == null
                || !emoteReactionMessage.getUsers().contains(event.getUserIdLong())
                || !emoteReactionMessage.getEmotes().containsKey(event.getReaction().getReactionEmote().getName())) {
            return;
        }

        if (emoteReactionMessage.isDeleteMessage()) {
            final MessageChannel channel = StatsBot.getDiscord().getTextChannelById(emoteReactionMessage.getChannelId());
            if (channel != null) {
                channel.deleteMessageById(event.getMessageIdLong()).queue();
            }

        } else if (emoteReactionMessage.isOneTimeUse()) {
            this.emoteMessageCache.invalidate(event.getMessageIdLong());
        }

        emoteReactionMessage.getEmotes().get(event.getReaction().getReactionEmote().getName()).onEmote();
    }

    @SubscribeEvent
    public void onMessageDelete(final MessageDeleteEvent event) {
        this.emoteMessageCache.invalidate(event.getMessageIdLong());
    }
}
