package de.timmi6790.statsbotdiscord.utilities;

public class UtilitiesString {
    public static String capitalize(final String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
