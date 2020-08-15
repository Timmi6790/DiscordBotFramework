package de.timmi6790.discord_framework.utilities;

import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;

import java.util.*;
import java.util.function.Function;

public class UtilitiesData {
    private static final SimilarityStrategy SIMILARITY_STRATEGY = new LevenshteinDistanceStrategy();

    public static List<String> getSimilarityList(final String source, final Collection<String> targets, final double minimumRate, final int limit) {
        return getSimilarityList(source, targets, String::toString, minimumRate, limit);
    }

    public static <T> List<T> getSimilarityList(final String source, final Collection<T> targets, final Function<T, String> toString, final double minimumRate, final int limit) {
        if (1 > limit) {
            return new ArrayList<>();
        }

        final String sourceLower = source.toLowerCase();
        final Map<Double, List<T>> sortedMap = new TreeMap<>(Collections.reverseOrder());
        for (final T target : targets) {
            final double value = SIMILARITY_STRATEGY.score(sourceLower, toString.apply(target).toLowerCase());
            if (minimumRate > value) {
                continue;
            }
            sortedMap.computeIfAbsent(value, d -> new ArrayList<>()).add(target);
        }

        final List<T> returnValues = new ArrayList<>();
        for (final Map.Entry<Double, List<T>> entry : sortedMap.entrySet()) {
            if (entry.getValue().size() + returnValues.size() >= limit) {
                returnValues.addAll(entry.getValue().subList(0, limit - returnValues.size()));
                break;
            } else {
                returnValues.addAll(entry.getValue());
            }
        }

        return returnValues;
    }
}
