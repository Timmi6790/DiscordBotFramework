package de.timmi6790.discord_framework.utilities.sentry;

import io.sentry.Breadcrumb;
import io.sentry.SentryLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BreadcrumbBuilder {
    private final Breadcrumb breadcrumb = new Breadcrumb();

    public Breadcrumb build() {
        return this.breadcrumb;
    }

    /**
     * Sets the message
     *
     * @param message the message
     */
    public BreadcrumbBuilder setMessage(@Nullable final String message) {
        this.breadcrumb.setMessage(message);
        return this;
    }

    /**
     * Sets the type
     *
     * @param type the type
     */
    public BreadcrumbBuilder setType(@Nullable final String type) {
        this.breadcrumb.setType(type);
        return this;
    }

    /**
     * Sets date.
     *
     * @param map the map
     * @return the date
     */
    public BreadcrumbBuilder setDate(final Map<String, Object> map) {
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            this.setData(entry.getKey(), entry.getValue());
        }

        return this;
    }

    /**
     * Sets an entry to the data's map
     *
     * @param key   the key
     * @param value the value
     */
    public BreadcrumbBuilder setData(@NotNull final String key, @NotNull final Object value) {
        this.breadcrumb.setData(key, value);
        return this;
    }

    /**
     * Removes an entry from the data's map
     *
     * @param key the key
     */
    public BreadcrumbBuilder removeData(@NotNull final String key) {
        this.breadcrumb.removeData(key);
        return this;
    }

    /**
     * Sets the category
     *
     * @param category the category
     */
    public BreadcrumbBuilder setCategory(@Nullable final String category) {
        this.breadcrumb.setCategory(category);
        return this;
    }

    /**
     * Sets the level
     *
     * @param level the level
     */
    public BreadcrumbBuilder setLevel(@Nullable final SentryLevel level) {
        this.breadcrumb.setLevel(level);
        return this;
    }
}
