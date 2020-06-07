package de.timmi6790.statsbotdiscord.modules.setting.settings;

import de.timmi6790.statsbotdiscord.modules.setting.AbstractSetting;

import java.util.Arrays;

public class BooleanSetting extends AbstractSetting<Boolean> {
    private static final String[] ALLOWED_VALUES = {"true", "false"};

    public BooleanSetting(final String internalName, final String name, final String defaultValues) {
        super(internalName, name, defaultValues);
    }

    @Override
    public Boolean parseSetting(final String setting) {
        return Boolean.parseBoolean(setting);
    }

    @Override
    public boolean isAllowedValue(final String value) {
        return Arrays.stream(ALLOWED_VALUES).anyMatch(allowedValue -> allowedValue.equalsIgnoreCase(value));
    }
}
