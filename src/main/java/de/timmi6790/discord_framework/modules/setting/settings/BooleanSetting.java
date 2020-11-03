package de.timmi6790.discord_framework.modules.setting.settings;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;

import java.util.Optional;

public class BooleanSetting extends AbstractSetting<Boolean> {
    private static final String[] ALLOWED_VALUES = {"true", "false", ""};

    public BooleanSetting(final String internalName, final String name, final String defaultValue) {
        super(internalName, name, defaultValue);
    }

    @Override
    public void handleCommand(final CommandParameters commandParameters, final String newValue) {

    }

    private Optional<Boolean> parseNewValue(final CommandParameters commandParameters, final String newValue) {
        if (newValue.equalsIgnoreCase("true")) {
            return Optional.of(true);
        } else if (newValue.equalsIgnoreCase("false")) {
            return Optional.of(false);
        }

        return Optional.empty();
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
