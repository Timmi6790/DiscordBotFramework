package de.timmi6790.discord_framework.module.modules.reactions.emote;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.reactions.common.cache.CacheExpireAfter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
public class EmoteReactionModule extends AbstractModule {
    private final Cache<Long, EmoteReaction> messageCache = Caffeine.newBuilder()
            .recordStats()
            .maximumSize(400)
            .expireAfter(new CacheExpireAfter<>())
            .build();

    public EmoteReactionModule() {
        super("EmoteReaction");

        this.addDependenciesAndLoadAfter(
                EventModule.class
        );

        // Register metrics
        DiscordBot.CACHE_METRICS.addCache("reaction_emote_message_cache", this.messageCache);
    }

    @Override
    public boolean onInitialize() {
        this.getModuleOrThrow(EventModule.class)
                .addEventListener(this);
        return true;
    }

    public void invalidateMessage(final long messageId) {
        this.messageCache.invalidate(messageId);
    }

    public Optional<EmoteReaction> getEmoteReaction(final long messageId) {
        return Optional.ofNullable(this.messageCache.getIfPresent(messageId));
    }

    public void addEmoteReactionMessage(@NonNull final Message message,
                                        @NonNull final EmoteReaction emoteReaction) {
        this.messageCache.put(message.getIdLong(), emoteReaction);
        for (final String emote : emoteReaction.getEmotes().keySet()) {
            message.addReaction(emote).queue(
                    null,
                    new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.MISSING_PERMISSIONS)
            );
        }
    }
}
