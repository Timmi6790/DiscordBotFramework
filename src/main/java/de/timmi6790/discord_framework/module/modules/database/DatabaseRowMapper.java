package de.timmi6790.discord_framework.module.modules.database;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Utility functions to convert the most common database returns.
 */
public abstract class DatabaseRowMapper {
    /**
     * Splits the input into a set of type T with the default splitter ,.
     *
     * @param <T>          wanted set type
     * @param string       database input string
     * @param typeFunction function to convert string input into wanted set type
     * @return the splitted set
     */
    protected <T> Set<T> toSet(@Nullable final String string,
                               @NonNull final Function<String, T> typeFunction) {
        return this.toSet(string, typeFunction, ",");
    }

    /**
     * Splits the input into a set of type T with the given splitter.
     *
     * @param <T>          wanted set type
     * @param string       database input string
     * @param typeFunction function to convert string input into wanted set type
     * @param splitter     the splitter
     * @return the splitted set
     */
    protected <T> Set<T> toSet(@Nullable final String string,
                               @NonNull final Function<String, T> typeFunction,
                               @NonNull final String splitter) {
        if (string == null) {
            return new HashSet<>();
        }

        final String[] rawValues = string.split(splitter);
        final Set<T> values = new HashSet<>(rawValues.length);
        for (final String rawValue : rawValues) {
            values.add(typeFunction.apply(rawValue));
        }
        return values;
    }


    /**
     * Converts the given database input into a map of K and V. The listSeparator defines how the input is split the
     * first time and the keyValueSeparator defines how the key and value is required to be split.
     *
     * @param <K>           key type parameter
     * @param <V>           value type parameter
     * @param string        database input string
     * @param keyFunction   function to convert string input into the wanted key type
     * @param valueFunction function to convert string input into the wanted value type
     * @return the parsed database map
     */
    protected <K, V> Map<K, V> toMap(@Nullable final String string,
                                     @NonNull final Function<String, K> keyFunction,
                                     @NonNull final Function<String, V> valueFunction) {
        return this.toMap(string, keyFunction, valueFunction, ";", ",");
    }

    /**
     * Converts the given database input into a map of K and V. The listSeparator defines how the input is split the
     * first time and the keyValueSeparator defines how the key and value is required to be split.
     *
     * @param <K>               key type parameter
     * @param <V>               value type parameter
     * @param string            database input string
     * @param keyFunction       function to convert string input into the wanted key type
     * @param valueFunction     function to convert string input into the wanted value type
     * @param listSeparator     the list separator
     * @param keyValueSeparator the key value separator
     * @return the parsed database map
     */
    protected <K, V> Map<K, V> toMap(@Nullable final String string,
                                     @NonNull final Function<String, K> keyFunction,
                                     @NonNull final Function<String, V> valueFunction,
                                     @NonNull final String listSeparator,
                                     @NonNull final String keyValueSeparator) {
        if (string == null) {
            return new HashMap<>();
        }

        final Map<K, V> result = new HashMap<>();
        for (final String listPart : string.split(listSeparator)) {
            final String[] keyValue = listPart.split(keyValueSeparator);
            if (keyValue.length == 2) {
                result.put(
                        keyFunction.apply(keyValue[0]),
                        valueFunction.apply(keyValue[1])
                );
            }
        }
        return result;
    }
}
