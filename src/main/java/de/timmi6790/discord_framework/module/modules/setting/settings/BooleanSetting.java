package de.timmi6790.discord_framework.module.modules.setting.settings;

import de.timmi6790.discord_framework.module.modules.command_old.CommandParameters;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BooleanSetting extends AbstractSetting<Boolean> {
    public BooleanSetting(final String name,
                          final String description,
                          final boolean defaultValue,
                          final String... aliasNames) {
        super(name, description, defaultValue, aliasNames);
    }

    @Override
    protected Optional<Boolean> parseNewValue(final CommandParameters commandParameters, final String userInput) {
        if ("true".equalsIgnoreCase(userInput)) {
            return Optional.of(Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(userInput)) {
            return Optional.of(Boolean.FALSE);
        }

        return Optional.empty();
    }

    @Override
    protected List<Boolean> possibleValues(final CommandParameters commandParameters, final String userInput) {
        return Arrays.asList(Boolean.TRUE, Boolean.FALSE);
    }

    @Override
    public String toDatabaseValue(final Boolean value) {
        return value ? String.valueOf(1) : String.valueOf(0);
    }

    @Override
    public Boolean fromDatabaseValue(final String value) {
        return "1".equals(value);
    }
}
