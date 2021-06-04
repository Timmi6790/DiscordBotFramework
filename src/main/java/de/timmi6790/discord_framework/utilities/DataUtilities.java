package de.timmi6790.discord_framework.utilities;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Data utilities.
 */
@UtilityClass
public class DataUtilities {
    private final SimilarityStrategy SIMILARITY_STRATEGY = new LevenshteinDistanceStrategy();

    /**
     * Returns values that are similar to the given source. The levenshtein distance strategy is used to compare all
     * values.
     *
     * @param source      source to match against values
     * @param values      values to check
     * @param minimumRate minimum inclusive similarity rate
     * @param limit       limit of returned elements
     * @return similar values
     */
    public List<String> getSimilarityList(@NonNull final String source,
                                          @NonNull final Collection<String> values,
                                          final double minimumRate,
                                          final int limit) {
        return getSimilarityList(source, values, String::toString, minimumRate, limit);
    }

    /**
     * Returns values that are similar to the given source. The levenshtein distance strategy is used to compare all
     * values.
     *
     * @param <T>         type parameter
     * @param source      source to match against values
     * @param values      values to check
     * @param toString    value to string function
     * @param minimumRate minimum inclusive similarity rate
     * @param limit       limit of returned elements
     * @return similar values
     */
    public <T> List<T> getSimilarityList(@NonNull final String source,
                                         @NonNull final Collection<T> values,
                                         @NonNull final Function<T, String> toString,
                                         final double minimumRate,
                                         final int limit) {
        if (1 > limit) {
            return new ArrayList<>();
        }

        final String sourceLower = source.toLowerCase();
        final Multimap<Double, T> sortedMap = MultimapBuilder
                .treeKeys(Collections.reverseOrder())
                .arrayListValues()
                .build();
        for (final T value : values) {
            final double similarityValue = SIMILARITY_STRATEGY.score(sourceLower, toString.apply(value).toLowerCase());
            if (similarityValue >= minimumRate) {
                sortedMap.put(similarityValue, value);
            }
        }

        final List<T> sortedValues = new ArrayList<>(sortedMap.values());
        return sortedValues.subList(0, Math.min(limit, sortedValues.size()));
    }

    /**
     * Converts the given collection into a string collection
     *
     * @param <T>        the type parameter
     * @param collection the collection
     * @param toString   to string function
     * @return the converted string list
     */
    public <T> List<String> convertToStringList(final Collection<T> collection, final Function<T, String> toString) {
        final List<String> result = new ArrayList<>(collection.size());
        for (final T value : collection) {
            result.add(toString.apply(value));
        }
        return result;
    }
}
