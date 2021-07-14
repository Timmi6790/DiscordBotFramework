package de.timmi6790.discord_framework.module.modules.command.utilities;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class ArrayUtilities {
    public <T> T[] modifyArrayAtPosition(final T[] array, final T value, final int position) {
        // Make sure that the array has enough positions
        final T[] newValues = Arrays.copyOf(array, Math.max(position + 1, array.length));
        newValues[position] = value;
        return newValues;
    }
}
