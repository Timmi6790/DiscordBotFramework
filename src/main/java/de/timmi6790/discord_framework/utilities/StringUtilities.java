package de.timmi6790.discord_framework.utilities;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtilities {
    public String capitalize(@NonNull final String string) {
        if (string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
