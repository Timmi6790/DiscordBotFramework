package de.timmi6790.discord_framework.modules.feedback;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.feedback.commands.FeedbackCommand;
import de.timmi6790.discord_framework.modules.feedback.feedbacks.BugFeedbackHandler;
import de.timmi6790.discord_framework.modules.feedback.feedbacks.SuggestionFeedbackHandler;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class FeedbackModule extends AbstractModule {
    @Getter
    private static final int FEEDBACK_TIME = 10;

    private final Map<String, FeedbackHandler> feedbackMap = new HashMap<>();

    @Getter
    private final Cache<Long, String> activeFeedbackCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(FEEDBACK_TIME, TimeUnit.MINUTES)
            .build();


    public FeedbackModule() {
        super("Feedback");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                ConfigModule.class,
                CommandModule.class,
                EmoteReactionModule.class,
                EventModule.class
        );

        this.addFeedbackHandlers(
                new BugFeedbackHandler(),
                new SuggestionFeedbackHandler()
        );
    }

    public List<FeedbackHandler> getFeedbackHandlers() {
        return new ArrayList<>(this.feedbackMap.values());
    }

    public Optional<FeedbackHandler> getFeedbackHandler(final String name) {
        return Optional.ofNullable(this.feedbackMap.get(name.toLowerCase()));
    }

    public boolean addFeedbackHandler(final FeedbackHandler feedbackHandler) {
        final String nameLower = feedbackHandler.getFeedbackName();
        if (this.feedbackMap.containsKey(nameLower)) {
            return false;
        }

        this.feedbackMap.put(nameLower, feedbackHandler);
        return true;
    }

    public void addFeedbackHandlers(final FeedbackHandler... feedbackHandlers) {
        Arrays.stream(feedbackHandlers).forEach(this::addFeedbackHandler);
    }

    @Override
    public void onInitialize() {
        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new FeedbackCommand()
        );

        this.getModuleOrThrow(EventModule.class).addEventListeners(
                new MessageListener()
        );
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
