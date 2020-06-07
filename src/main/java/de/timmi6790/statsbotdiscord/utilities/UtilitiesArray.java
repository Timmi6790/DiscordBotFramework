package de.timmi6790.statsbotdiscord.utilities;

public class UtilitiesArray {
    public static boolean isEmpty(final String[] array) {
        if (array == null) {
            return true;
        }

        for (final String content : array) {
            if (!content.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
