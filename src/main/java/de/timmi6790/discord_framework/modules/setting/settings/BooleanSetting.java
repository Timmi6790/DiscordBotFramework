package de.timmi6790.discord_framework.modules.setting.settings;

import de.timmi6790.discord_framework.modules.setting.AbstractSetting;

import java.util.Arrays;

public class BooleanSetting extends AbstractSetting<Boolean> {
    private static final String[] ALLOWED_VALUES = {"true", "false"};

    public BooleanSetting(final String internalName, final String name, final String defaultValues) {
        super(internalName, name, defaultValues);
    }
    

    @Override
    public boolean isAllowedValue(final String value) {
        return Arrays.stream(ALLOWED_VALUES).anyMatch(allowedValue -> allowedValue.equalsIgnoreCase(value));
    }

    @Override
    public String toDatabaseValue(Boolean value) {
        return null;
    }

    @Override
    public Boolean fromDatabaseValue(String value) {
        return Boolean.parseBoolean(value);
    }
}
