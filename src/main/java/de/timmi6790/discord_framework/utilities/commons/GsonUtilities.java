package de.timmi6790.discord_framework.utilities.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

/**
 * File utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GsonUtilities {
    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    /**
     * Saves the newVersion to the path in json format, when the oldVersion is not equal to the newVersion.
     *
     * @param <T>        the type parameter
     * @param path       the path
     * @param oldVersion the old object version
     * @param newVersion the new object version
     * @return success status
     */
    public static <T> boolean saveToJsonIfChanged(@NonNull final Path path,
                                                  @NonNull final T oldVersion,
                                                  @NonNull final T newVersion) {
        if (oldVersion.equals(newVersion)) {
            return false;
        }

        saveToJson(path, newVersion);
        return true;
    }

    /**
     * Saves the object to the path in json format.
     *
     * @param <T>    the type parameter
     * @param path   the path
     * @param object the object
     */
    public static <T> void saveToJson(@NonNull final Path path, @NonNull final T object) {
        try {
            Files.write(path, Collections.singleton(gson.toJson(object)));
        } catch (final IOException e) {
            // Logger.error(e, "Error while trying to save file.");
        }
    }

    /**
     * Reads the path as json.
     *
     * @param <T>   the type parameter
     * @param path  the path
     * @param clazz the clazz of the json file
     * @return the parsed object
     */
    public static <T> Optional<T> readJsonFile(@NonNull final Path path, @NonNull final Class<T> clazz) {
        try (final BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(gson.fromJson(bufferedReader, clazz));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }
}
