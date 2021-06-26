package de.timmi6790.discord_framework.utilities.commons;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Several reflection related utility functions.
 */
@UtilityClass
public final class ReflectionUtilities {

    /**
     * Convenient utility function that will search for the given annotation for the method, instead of throwing a
     * NullPointerException.
     *
     * @param <T>             the required annotation
     * @param method          the method of the wanted annotation
     * @param annotationClass the required annotation class
     * @return the found annotation
     */
    public <T extends Annotation> Optional<T> getAnnotation(final Method method, final Class<T> annotationClass) {
        try {
            final T annotation = method.getAnnotation(annotationClass);
            return Optional.of(annotation);
        } catch (final NullPointerException ignore) {
            return Optional.empty();
        }
    }
}
