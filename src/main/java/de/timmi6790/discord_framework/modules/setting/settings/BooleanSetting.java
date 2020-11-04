package de.timmi6790.discord_framework.modules.setting.settings;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;
import java.util.Optional;

public class BooleanSetting extends AbstractSetting<Boolean> {
    public BooleanSetting(final String internalName, final String name, final String description, final boolean defaultValue) {
        super(internalName, name, description, defaultValue ? String.valueOf(1) : String.valueOf(0));
    }

    @Override
    protected Optional<Boolean> parseNewValue(final CommandParameters commandParameters, final String userInput) {
        if (userInput.equalsIgnoreCase("true")) {
            return Optional.of(true);
        } else if (userInput.equalsIgnoreCase("false")) {
            return Optional.of(false);
        }

        return Optional.empty();
    }

    @Override
    protected List<Boolean> possibleValues(final CommandParameters commandParameters, final String userInput) {
        return Arrays.asList(new Boolean[]{true, false});
    }

    @Override
    public String toDatabaseValue(final Boolean value) {
        return value ? String.valueOf(1) : String.valueOf(0);
    }

    @Override
    public Boolean fromDatabaseValue(final String value) {
        return value.equals("1");
    }
}
