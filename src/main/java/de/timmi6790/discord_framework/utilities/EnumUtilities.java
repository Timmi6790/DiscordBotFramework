package de.timmi6790.discord_framework.utilities;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnumUtilities {
    public static List<String> getPrettyNames(final Enum[] enumValue) {
        return Arrays.stream(enumValue)
                .map(EnumUtilities::getPrettyName)
                .collect(Collectors.toList());
    }

    public static String getPrettyName(final Enum enumValue) {
        return getPrettyName(enumValue.name());
    }

    public static String getPrettyName(final String enumName) {
        if (enumName.isEmpty()) {
            return enumName;
        }

        // Remove _ and capitalize after the first part
        final String[] nameParts = enumName.split("_");
        final StringBuilder prettyName = new StringBuilder(nameParts[0].toLowerCase());
        for (int index = 1; nameParts.length > index; index++) {
            prettyName.append(UtilitiesString.capitalize(nameParts[index].toLowerCase()));
        }

        return prettyName.toString();
    }

    public static <T extends Enum> Optional<T> getIgnoreCase(final String required, final T[] enumValue) {
        return Arrays.stream(enumValue)
                .filter(value -> getPrettyName(value).equalsIgnoreCase(required))
                .findAny();
    }
}
