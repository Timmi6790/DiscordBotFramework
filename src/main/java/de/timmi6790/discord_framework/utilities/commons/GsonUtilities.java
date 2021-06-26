package de.timmi6790.discord_framework.utilities.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

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
@UtilityClass
@Log4j2
public class GsonUtilities {
    private final Gson gson = new GsonBuilder()
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
    public <T> boolean saveToJsonIfChanged(@NonNull final Path path,
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
    public <T> void saveToJson(@NonNull final Path path, @NonNull final T object) {
        try {
            Files.write(path, Collections.singleton(gson.toJson(object)));
        } catch (final IOException e) {
            log.error("Error while trying to save file.", e);
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
    public <T> Optional<T> readJsonFile(@NonNull final Path path, @NonNull final Class<T> clazz) {
        try (final BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(gson.fromJson(bufferedReader, clazz));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }
}
