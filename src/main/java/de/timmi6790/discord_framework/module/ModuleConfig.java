package de.timmi6790.discord_framework.module;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ModuleConfig {
    private final Map<String, Boolean> enabledModules = new HashMap<>();
}
