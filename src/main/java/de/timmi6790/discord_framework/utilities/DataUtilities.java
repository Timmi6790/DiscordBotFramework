package de.timmi6790.discord_framework.utilities;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataUtilities {
    private static final SimilarityStrategy SIMILARITY_STRATEGY = new LevenshteinDistanceStrategy();

    public static List<String> getSimilarityList(final String source, final Collection<String> targets, final double minimumRate, final int limit) {
        return getSimilarityList(source, targets, String::toString, minimumRate, limit);
    }

    public static <T> List<T> getSimilarityList(final String source, final Collection<T> targets, final Function<T, String> toString, final double minimumRate, final int limit) {
        if (1 > limit) {
            return new ArrayList<>();
        }

        final String sourceLower = source.toLowerCase();
        final Multimap<Double, T> sortedMap = MultimapBuilder.
                treeKeys(Collections.reverseOrder())
                .arrayListValues()
                .build();
        for (final T target : targets) {
            final double value = SIMILARITY_STRATEGY.score(sourceLower, toString.apply(target).toLowerCase());
            if (value >= minimumRate) {
                sortedMap.put(value, target);
            }
        }

        return sortedMap.values()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
