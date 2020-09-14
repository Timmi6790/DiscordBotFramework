package de.timmi6790.discord_framework.utilities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtilities {
    public String capitalize(final String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
