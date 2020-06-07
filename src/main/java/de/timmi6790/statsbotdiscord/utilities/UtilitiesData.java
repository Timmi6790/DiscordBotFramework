package de.timmi6790.statsbotdiscord.utilities;

import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UtilitiesData {
    private static final SimilarityStrategy SIMILARITY_STRATEGY = new LevenshteinDistanceStrategy();
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");

    public static List<String> getSimilarityList(final String source, final Collection<String> targets, final double minimumRate, final int limit) {
        final String sourceLower = source.toLowerCase();
        return targets.parallelStream()
                .filter(value -> UtilitiesData.SIMILARITY_STRATEGY.score(sourceLower, value.toLowerCase()) >= minimumRate)
                .sorted(Comparator.comparingDouble((value) -> UtilitiesData.SIMILARITY_STRATEGY.score(sourceLower, value.toLowerCase()) * -1))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static <T> List<T> getSimilarityList(final String source, final Collection<T> targets, final Function<T, String> toString, final double minimumRate, final int limit) {
        final String sourceLower = source.toLowerCase();
        return targets.parallelStream()
                .filter(value -> UtilitiesData.SIMILARITY_STRATEGY.score(sourceLower, toString.apply(value).toLowerCase()) >= minimumRate)
                .sorted(Comparator.comparingDouble((value) -> UtilitiesData.SIMILARITY_STRATEGY.score(sourceLower, toString.apply(value).toLowerCase()) * -1))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static boolean isInt(final Object value) {
        return UtilitiesData.INTEGER_PATTERN.matcher(String.valueOf(value)).matches();
    }
}
