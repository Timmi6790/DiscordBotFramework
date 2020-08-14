package de.timmi6790.discord_framework.utilities;

import com.google.gson.Gson;
import lombok.SneakyThrows;

import java.lang.reflect.Type;

public class ReflectionUtilities {
    @SneakyThrows
    public static <O> O deepCopy(final O object) {
        // TODO: Implement a version based on reflections
        final Gson gson = new Gson();
        return gson.fromJson(gson.toJson(object), (Type) object.getClass());
    }
}
