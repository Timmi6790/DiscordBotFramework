package de.timmi6790.discord_framework.modules.emote_reaction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.emote_reaction.cache.CacheExpireAfter;
import de.timmi6790.discord_framework.modules.emote_reaction.cache.CacheRemoveListener;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@EqualsAndHashCode(callSuper = true)
@Getter
public class EmoteReactionModule extends AbstractModule {
    private static final int ACTIVE_EMOTES_LIMIT = 6;

    private final Map<Long, AtomicInteger> activeEmotesPerPlayer = new ConcurrentHashMap<>();
    private final Cache<Long, EmoteReactionMessage> emoteMessageCache = Caffeine.newBuilder()
            .recordStats()
            .maximumSize(10_000)
            .expireAfter(new CacheExpireAfter())
            .removalListener(new CacheRemoveListener(this))
            .build();

    public EmoteReactionModule() {
        super("EmoteReaction");

        this.addDependenciesAndLoadAfter(
                EventModule.class
        );

        // Register metrics
        DiscordBot.CACHE_METRICS.addCache("emote_reaction_message_cache", this.emoteMessageCache);
    }

    @Override
    public boolean onInitialize() {
        this.getModuleOrThrow(EventModule.class).addEventListener(this);
        return true;
    }

    public void addEmoteReactionMessage(@NonNull final Message message,
                                        @NonNull final EmoteReactionMessage emoteReactionMessage) {
        // Remove all players who reached the rate limit
        emoteReactionMessage.getUsers()
                .removeIf(user -> {
                    final AtomicInteger currentActive = this.activeEmotesPerPlayer.
                            computeIfAbsent(user, k -> new AtomicInteger(0));
                    if (currentActive.get() >= ACTIVE_EMOTES_LIMIT) {
                        return true;
                    }

                    currentActive.incrementAndGet();
                    return false;
                });

        if (emoteReactionMessage.getUsers().isEmpty()) {
            return;
        }

        this.emoteMessageCache.put(message.getIdLong(), emoteReactionMessage);
        for (final String emote : emoteReactionMessage.getEmotes().keySet()) {
            message.addReaction(emote).queue(
                    null,
                    new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.MISSING_PERMISSIONS)
            );
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
            final MessageChannel channel = this.getDiscord().getTextChannelById(emoteReactionMessage.getChannelId());
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
