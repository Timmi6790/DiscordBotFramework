package de.timmi6790.discord_framework.module.modules.reactions.button;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.listeners.ButtonReactionListener;
import de.timmi6790.discord_framework.module.modules.reactions.common.cache.CacheExpireAfter;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class ButtonReactionModule extends AbstractModule {
    private final Cache<Long, ButtonReaction> messageCache = Caffeine.newBuilder()
            .recordStats()
            .maximumSize(400)
            .expireAfter(new CacheExpireAfter<>())
            .build();

    public ButtonReactionModule() {
        super("ButtonReaction");

        this.addDependenciesAndLoadAfter(
                EventModule.class
        );
        
        this.addLoadAfterDependencies(
                MetricModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.getModuleOrThrow(EventModule.class)
                .addEventListener(
                        new ButtonReactionListener(this)
                );

        // Register metrics
        this.getModule(MetricModule.class).ifPresent(metric ->
                CaffeineCacheMetrics.monitor(
                        metric.getMeterRegistry(),
                        this.messageCache,
                        "reaction_button_message"
                )
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
