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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class FeedbackModule extends AbstractModule {
    @Getter
    private static final int FEEDBACK_TIME = 10;

    private final Map<String, FeedbackHandler> feedbackMap = new CaseInsensitiveMap<>();

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
                new BugFeedbackHandler(this)
                //new SuggestionFeedbackHandler()
        );
    }

    public List<FeedbackHandler> getFeedbackHandlers() {
        return new ArrayList<>(this.feedbackMap.values());
    }

    public Optional<FeedbackHandler> getFeedbackHandler(final String name) {
        return Optional.ofNullable(this.feedbackMap.get(name));
    }

    public boolean addFeedbackHandler(final FeedbackHandler feedbackHandler) {
        if (this.feedbackMap.containsKey(feedbackHandler.getFeedbackName())) {
            return false;
        }

        this.feedbackMap.put(feedbackHandler.getFeedbackName(), feedbackHandler);
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
                new MessageListener(this)
        );

        this.getModuleOrThrow(ConfigModule.class).registerConfig(
                this,
                new Config()
        );
    }

    @Override
    public void onEnable() {
        final Config config = this.getConfig();

        for (final FeedbackHandler handler : this.feedbackMap.values()) {
            config.getFeedbackConfigs().putIfAbsent(handler.getFeedbackName(), new Config.ChannelFeedbackConfig(-1L));
        }

        this.getModuleOrThrow(ConfigModule.class).saveConfig(this, Config.class);
    }

    public Config getConfig() {
        return this.getModuleOrThrow(ConfigModule.class).getConfig(this, Config.class);
    }
}
