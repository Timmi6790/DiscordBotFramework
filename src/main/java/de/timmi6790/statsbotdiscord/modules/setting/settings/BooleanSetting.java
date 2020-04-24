package de.timmi6790.statsbotdiscord.modules.setting.settings;

import de.timmi6790.statsbotdiscord.modules.setting.AbstractSetting;

public class BooleanSetting extends AbstractSetting<Boolean> {
    public BooleanSetting(final String internalName, final String name, final boolean requirePerms, final String permNode, final String defaultValues) {
        super(internalName, name, requirePerms, permNode, defaultValues);
    }

    @Override
    public Boolean parseSetting(final String setting) {
        return Boolean.parseBoolean(setting);
    }
}
