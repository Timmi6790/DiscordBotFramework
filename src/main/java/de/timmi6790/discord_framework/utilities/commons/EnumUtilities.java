package de.timmi6790.discord_framework.utilities.commons;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Enum utilities.
 */
@UtilityClass
public class EnumUtilities {
    /**
     * Converts all enum value into a more readable name.
     *
     * @param enumValue the enum value
     * @return the pretty names
     */
    public List<String> getPrettyNames(@NonNull final Enum<?>[] enumValue) {
        return ListUtilities.toStringList(
                Arrays.asList(enumValue.clone()),
                EnumUtilities::getPrettyName
        );
    }

    /**
     * Converts a enum value into a neat formatted text. It will remove all _ characters and will also capitalize all
     * values after the first part. TEST_Value -> TestValue
     *
     * @param enumValue the enum value
     * @return the pretty name
     */
    public String getPrettyName(@NonNull final Enum<?> enumValue) {
        // Remove _ and capitalize after the first part
        final String[] nameParts = enumValue.name().split("_");
        final StringBuilder prettyName = new StringBuilder();
        for (final String namePart : nameParts) {
            prettyName.append(StringUtilities.capitalize(namePart.toLowerCase()));
        }

        return prettyName.toString();
    }

    /**
     * Searches for the search string against the given enum values. All enum values are checked with their pretty name
     * {@link #getPrettyName(Enum)}}.
     *
     * @param <T>        the enum type
     * @param search     the search string
     * @param enumValues the enum value
     * @return the found enum value
     */
    public <T extends Enum<?>> Optional<T> getIgnoreCase(@NonNull final String search, @NonNull final T[] enumValues) {
        for (final T value : enumValues) {
            if (getPrettyName(value).equalsIgnoreCase(search)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
