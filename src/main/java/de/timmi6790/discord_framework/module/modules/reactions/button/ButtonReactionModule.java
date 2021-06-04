package de.timmi6790.discord_framework.module.modules.reactions.button;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.cache.CacheExpireAfter;
import de.timmi6790.discord_framework.module.modules.reactions.button.listener.ButtonReactionListener;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;

public class ButtonReactionModule extends AbstractModule {
    private final Cache<Long, ButtonReaction> messageCache = Caffeine.newBuilder()
            .recordStats()
            .maximumSize(400)
            .expireAfter(new CacheExpireAfter())
            .build();

    public ButtonReactionModule() {
        super("ButtonReaction");

        this.addDependenciesAndLoadAfter(
                EventModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.getModuleOrThrow(EventModule.class)
                .addEventListener(
                        new ButtonReactionListener(this)
                );
        return true;
    }

    public Optional<ButtonReaction> getButtonReaction(final long messageId) {
        return Optional.ofNullable(this.messageCache.getIfPresent(messageId));
    }

    public void invalidateMessage(final long messageId) {
        this.messageCache.invalidate(messageId);
    }

    public void addButtonReactionMessage(@NonNull final Message message,
                                         @NonNull final ButtonReaction buttonReaction) {
        this.addButtonReactionMessage(message.getIdLong(), buttonReaction);
    }

    public void addButtonReactionMessage(final long messageID,
                                         @NonNull final ButtonReaction buttonReaction) {
        this.messageCache.put(messageID, buttonReaction);
    }
}
