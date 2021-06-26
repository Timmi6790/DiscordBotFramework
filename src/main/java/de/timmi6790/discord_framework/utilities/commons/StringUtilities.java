package de.timmi6790.discord_framework.utilities.commons;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * String utilities.
 */
@UtilityClass
public class StringUtilities {
    /**
     * Capitalize the input string.
     *
     * @param string the string
     * @return the capitalized string
     */
    public String capitalize(@NonNull final String string) {
        if (string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
