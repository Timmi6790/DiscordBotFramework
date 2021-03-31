package de.timmi6790.discord_framework.module.modules.setting.settings;

import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;

public abstract class StringSetting extends AbstractSetting<String> {
    protected StringSetting(final String name,
                            final String description,
                            final String defaultValues,
                            final String... aliasNames) {
        super(name, description, defaultValues, aliasNames);
    }
}
