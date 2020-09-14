package de.timmi6790.discord_framework.modules.database;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    protected <T> Set<T> toSet(final String string, final Function<String, T> typeFunction) {
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
    protected <T> Set<T> toSet(final String string, final Function<String, T> typeFunction, final String splitter) {
        if (string == null) {
            return new HashSet<>();
        }

        return Arrays.stream(string.split(splitter))
                .map(typeFunction)
                .collect(Collectors.toSet());
    }


    /**
     * Converts the given database input into a map of K and V.
     * The listSeparator defines how the input is split the first time and
     * the keyValueSeparator defines how the key and value is required to be split.
     *
     * @param <K>           key type parameter
     * @param <V>           value type parameter
     * @param string        database input string
     * @param keyFunction   function to convert string input into the wanted key type
     * @param valueFunction function to convert string input into the wanted value type
     * @return the parsed database map
     */
    protected <K, V> Map<K, V> toMap(final String string, final Function<String, K> keyFunction, final Function<String, V> valueFunction) {
        return this.toMap(string, keyFunction, valueFunction, ";", ",");
    }

    /**
     * Converts the given database input into a map of K and V.
     * The listSeparator defines how the input is split the first time and
     * the keyValueSeparator defines how the key and value is required to be split.
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
    protected <K, V> Map<K, V> toMap(final String string, final Function<String, K> keyFunction, final Function<String, V> valueFunction,
                                     final String listSeparator, final String keyValueSeparator) {
        if (string == null) {
            return new HashMap<>();
        }

        return Arrays.stream(string.split(listSeparator))
                .map(setting -> setting.split(keyValueSeparator))
                .filter(values -> values.length == 2)
                .collect(Collectors.toMap(values -> keyFunction.apply(values[0]), values -> valueFunction.apply(values[1])));
    }
}
