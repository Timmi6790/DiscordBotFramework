package de.timmi6790.discord_framework.utilities;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

/**
 * Several reflection related utility functions.
 */
@UtilityClass
public class ReflectionUtilities {
    /**
     * Creates a deep clone of the given object.
     *
     * @param <O>    the object you want to copy
     * @param object the object you want to copy
     * @return the deep cloned object
     */
    @SneakyThrows
    public <O> O deepCopy(final O object) {
        // TODO: Implement a version based on reflections
        final Gson gson = new Gson();
        return gson.fromJson(gson.toJson(object), (Type) object.getClass());
    }

    /**
     * Convenient utility function that will search for the given annotation for the method, instead of throwing a NullPointerException.
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

    /**
     * Adds a jar file to the given classLoader.
     *
     * @param jar            the jar
     * @param urlClassLoader the url class loader
     * @throws MalformedURLException     the malformed url exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException    the illegal access exception
     * @throws NoSuchMethodException     the no such method exception
     */
    public void addJarToClassLoader(final File jar, final URLClassLoader urlClassLoader) throws MalformedURLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(urlClassLoader, jar.toURI().toURL());
    }

    /**
     * Tries to load the given class path from the given urlClassLoader.
     * Instead of throwing a ClassNotFoundException, returns Optional::emtpy
     *
     * @param path           the path
     * @param urlClassLoader the url class loader
     * @return the optional
     */
    public Optional<Class<?>> loadClassFromClassLoader(final String path, final URLClassLoader urlClassLoader) {
        try {
            return Optional.of(urlClassLoader.loadClass(path));
        } catch (final ClassNotFoundException ignore) {
            return Optional.empty();
        }
    }
}
