package de.timmi6790.discord_framework.utilities.commons;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * String utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtilities {
    /**
     * Capitalize the input string.
     *
     * @param string the string
     * @return the capitalized string
     */
    public static String capitalize(@NonNull final String string) {
        if (string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
