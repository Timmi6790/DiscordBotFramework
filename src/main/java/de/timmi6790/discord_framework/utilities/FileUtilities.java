package de.timmi6790.discord_framework.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.DiscordBot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtilities {
    @Getter
    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static <T> boolean saveToJsonIfChanged(final Path path, final T oldVersion, final T newVersion) {
        if (oldVersion.equals(newVersion)) {
            return false;
        }

        saveToJson(path, newVersion);
        return true;
    }

    public static <T> void saveToJson(final Path path, final T object) {
        try {
            Files.write(path, Collections.singleton(gson.toJson(object)));
        } catch (final IOException e) {
            DiscordBot.getLogger().error(e, "Error while trying to save file.");
        }
    }

    @SneakyThrows
    public static <T> T readJsonFile(final Path path, final Class<T> clazz) {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toString()));
        return FileUtilities.getGson().fromJson(bufferedReader, clazz);
    }
}
