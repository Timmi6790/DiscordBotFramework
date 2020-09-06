package de.timmi6790.discord_framework.utilities;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtilities {
    private static URLClassLoader getSystemClassLoader() {
        return (URLClassLoader) ClassLoader.getSystemClassLoader();
    }

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

    public static Optional<Method> getMethod(final Class clazz, final String name, final Class<?>... parameterTypes) {
        try {
            return Optional.of(clazz.getMethod(name, parameterTypes));
        } catch (final NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public static void addJarToSystemClassLoader(final File jar) throws MalformedURLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Method method = getMethod(URLClassLoader.class, "addUrl", URL.class).orElseThrow(NoSuchMethodException::new);
        method.setAccessible(true);
        method.invoke(getSystemClassLoader(), jar.toURI().toURL());
    }

    public static Optional<Class<?>> loadClassFromSystemClassLoader(final String path) {
        try {
            return Optional.of(getSystemClassLoader().loadClass(path));
        } catch (final ClassNotFoundException ignore) {
            return Optional.empty();
        }
    }
}
