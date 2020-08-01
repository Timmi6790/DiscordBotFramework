package de.timmi6790.discord_framework.modules.setting.settings;

import de.timmi6790.discord_framework.modules.setting.AbstractSetting;

public abstract class StringSetting extends AbstractSetting<String> {
    public StringSetting(final String internalName, final String name, final String defaultValues) {
        super(internalName, name, defaultValues);
    }

}
