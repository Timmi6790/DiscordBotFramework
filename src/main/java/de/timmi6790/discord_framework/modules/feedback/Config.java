package de.timmi6790.discord_framework.modules.feedback;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Config {
    private final Map<String, ChannelFeedbackConfig> feedbackConfigs = new HashMap<>();

    @Data
    public static class ChannelFeedbackConfig {
        private final long channelId;
    }
}
