package de.timmi6790.discord_framework.utilities;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@UtilityClass
public class ModuleUtilities {
    public void addJarToSystemClassLoader(final File file) throws Exception {
        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
    }
}
