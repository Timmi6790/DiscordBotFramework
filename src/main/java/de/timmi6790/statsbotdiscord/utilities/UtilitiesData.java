package de.timmi6790.statsbotdiscord.utilities;

import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UtilitiesData {
    private static final SimilarityStrategy SIMILARITY_STRATEGY = new LevenshteinDistanceStrategy();

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[+-]?(\\d+|\\d+\\.\\d+|\\.\\d+|\\d+\\.)([eE]\\d+)?");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    public static List<String> getSimilarityList(final String source, final Collection<String> targets, final double minimumRate) {
        final String sourceLower = source.toLowerCase();
        return targets.parallelStream()
                .filter(value -> UtilitiesData.SIMILARITY_STRATEGY.score(sourceLower, value.toLowerCase()) >= minimumRate)
                .sorted(Comparator.comparingDouble((value) -> UtilitiesData.SIMILARITY_STRATEGY.score(sourceLower, value.toLowerCase()) * -1))
                .collect(Collectors.toList());
    }

    public static boolean hasArgInArgsEqualIgnoreCase(final String arg, final String... args) {
        for (final String a : args) {
            if (arg.equalsIgnoreCase(a)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInt(final Object value) {
        return UtilitiesData.INTEGER_PATTERN.matcher(String.valueOf(value)).matches();
    }

    public static boolean isLong(final Object value) {
        try {
            final long lValue = Long.parseLong(String.valueOf(value));
            return true;
        } catch (final Exception ignore) {
            return false;
        }
    }

    public static boolean isDouble(final Object value) {
        return UtilitiesData.DOUBLE_PATTERN.matcher(String.valueOf(value)).matches();
    }

    public static boolean isFloat(final Object value) {
        return UtilitiesData.FLOAT_PATTERN.matcher(String.valueOf(value)).matches();
    }

    public static boolean isBoolean(final Object value) {
        return UtilitiesData.BOOLEAN_PATTERN.matcher(String.valueOf(value)).matches();
    }
}
