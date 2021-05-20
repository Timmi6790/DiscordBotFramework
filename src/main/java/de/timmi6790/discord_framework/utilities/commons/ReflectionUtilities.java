package de.timmi6790.discord_framework.utilities.commons;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Several reflection related utility functions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtilities {
    /**
     * Creates a deep clone of the given object.
     *
     * @param <O>    the object you want to copy
     * @param object the object you want to copy
     * @return the deep cloned object
     */
    @SneakyThrows
    public static <O> O deepCopy(final O object) {
        // TODO: Implement a version based on reflections
        final Gson gson = new Gson();
        return gson.fromJson(gson.toJson(object), (Type) object.getClass());
    }

    /**
     * Convenient utility function that will search for the given annotation for the method, instead of throwing a
     * NullPointerException.
     *
     * @param <T>             the required annotation
     * @param method          the method of the wanted annotation
     * @param annotationClass the required annotation class
     * @return the found annotation
     */
    public static <T extends Annotation> Optional<T> getAnnotation(final Method method, final Class<T> annotationClass) {
        try {
            final T annotation = method.getAnnotation(annotationClass);
            return Optional.of(annotation);
        } catch (final NullPointerException ignore) {
            return Optional.empty();
        }
    }
}
