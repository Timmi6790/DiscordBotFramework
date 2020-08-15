package de.timmi6790.discord_framework.utilities;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)

public class ReflectionUtilities {
    @SneakyThrows
    public static <O> O deepCopy(final O object) {
        // TODO: Implement a version based on reflections
        final Gson gson = new Gson();
        return gson.fromJson(gson.toJson(object), (Type) object.getClass());
    }

    public static <T extends Annotation> Optional<T> getAnnotation(final Method method, final Class<T> annotationClass) {
        try {
            final T annotation = method.getAnnotation(annotationClass);
            return Optional.of(annotation);
        } catch (final NullPointerException ignore) {
            return Optional.empty();
        }
    }
}
