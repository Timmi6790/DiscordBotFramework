package de.timmi6790.discord_framework.modules.setting.settings;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;

import java.util.Arrays;
import java.util.Optional;

public class BooleanSetting extends AbstractSetting<Boolean> {
    private static final String[] ALLOWED_VALUES = {"true", "false", ""};

    public BooleanSetting(final String internalName, final String name, final String defaultValues) {
        super(internalName, name, defaultValues);
    }

    @Override
    public void handleCommand(final CommandParameters commandParameters, final String newValue) {

    }

    private Optional<Boolean> parseNewValue(final CommandParameters commandParameters, final String newValue) {
        if (newValue.isEmpty()) {
            // Reverse value
        }

        if (newValue.equalsIgnoreCase("true")) {
            return Optional.of(true);
        } else if (newValue.equalsIgnoreCase("false")) {
            return Optional.of(false);
        }

        return Optional.empty();
    }

    public boolean isAllowedValue(final String value) {
        return Arrays.stream(ALLOWED_VALUES).anyMatch(allowedValue -> allowedValue.equalsIgnoreCase(value));
    }

    @Override
    public String toDatabaseValue(final Boolean value) {
        return null;
    }

    @Override
    public Boolean fromDatabaseValue(final String value) {
        return Boolean.parseBoolean(value);
    }
}
