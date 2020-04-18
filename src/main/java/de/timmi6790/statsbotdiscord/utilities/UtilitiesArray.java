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

    public static int getSum(final int[] array) {
        int sum = 0;
        for (final int num : array) {
            sum += num;
        }
        return sum;
    }
}
