package de.timmi6790.discord_framework.modules.new_module_manager.dpi;

import lombok.Data;

@Data
public class DependencyInfo {
    private final Class<?> dependency;
    private final boolean optional;
}
