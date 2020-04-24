package de.timmi6790.statsbotdiscord.modules.setting.settings;

import de.timmi6790.statsbotdiscord.modules.setting.AbstractSetting;

public class StringSetting extends AbstractSetting<String> {
    public StringSetting(final String internalName, final String name, final boolean requirePerms, final String permNode, final String defaultValues) {
        super(internalName, name, requirePerms, permNode, defaultValues);
    }

    @Override
    public String parseSetting(final String setting) {
        return setting;
    }
}
