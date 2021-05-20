package de.timmi6790.discord_framework.utilities.commons;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * List utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ListUtilities {
    /**
     * Converts the values into a string list
     *
     * @param <T>              type parameter of the values list
     * @param values           the values
     * @param toStringFunction values to string function
     * @return the string list
     */
    public static <T> List<String> toStringList(final Collection<T> values, final Function<T, String> toStringFunction) {
        return toTypeList(values, toStringFunction);
    }

    /**
     * Convert the input collection to a type list
     *
     * @param <C>            type parameter of the input collection
     * @param <T>            type parameter of the output list
     * @param values         input values
     * @param toTypeFunction input type to output type function
     * @return the converted list
     */
    public static <C, T> List<T> toTypeList(final Collection<C> values, final Function<C, T> toTypeFunction) {
        final List<T> convertedList = new ArrayList<>(values.size());
        for (final C value : values) {
            convertedList.add(toTypeFunction.apply(value));
        }
        return convertedList;
    }
}
