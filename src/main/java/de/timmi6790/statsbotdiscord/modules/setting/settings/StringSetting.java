package de.timmi6790.statsbotdiscord.modules.setting.settings;

import de.timmi6790.statsbotdiscord.modules.setting.AbstractSetting;

public abstract class StringSetting extends AbstractSetting<String> {
    public StringSetting(final String internalName, final String name, final String defaultValues) {
        super(internalName, name, defaultValues);
    }

    @Override
    public String parseSetting(final String setting) {
        return setting;
    }
}
