package de.timmi6790.discord_framework;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Config {
    private String discordToken = "";
    private String sentry = "";
    private Map<String, Boolean> enabledModules = new HashMap<>();
}
