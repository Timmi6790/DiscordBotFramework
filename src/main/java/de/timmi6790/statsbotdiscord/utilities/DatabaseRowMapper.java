package de.timmi6790.statsbotdiscord.utilities;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DatabaseRowMapper {
    protected <T> Set<T> toSet(final String string, final Function<String, T> typeFunction) {
        if (string == null) {
            return new HashSet<>();
        }

        return Arrays.stream(string.split(","))
                .map(typeFunction)
                .collect(Collectors.toSet());
    }

    public <K, V> Map<K, V> toMap(final String string, final Function<String, K> keyFunction, final Function<String, V> valueFunction) {
        if (string == null) {
            return new HashMap<>();
        }

        return Arrays.stream(string.split(";"))
                .map(setting -> setting.split(","))
                .filter(values -> values.length == 2)
                .collect(Collectors.toMap(values -> keyFunction.apply(values[0]), values -> valueFunction.apply(values[1])));
    }
}
