package de.timmi6790.discord_framework.utilities.sentry;

import io.sentry.SentryLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BreadcrumbBuilderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setMessage(final String message) {
        final BreadcrumbBuilder breadcrumbBuilder = new BreadcrumbBuilder();
        breadcrumbBuilder.setMessage(message);

        final String foundMessage = breadcrumbBuilder.build().getMessage();
        assertThat(foundMessage).isEqualTo(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Type"})
    void setType(final String type) {
        final BreadcrumbBuilder breadcrumbBuilder = new BreadcrumbBuilder();
        breadcrumbBuilder.setType(type);

        final String foundType = breadcrumbBuilder.build().getType();
        assertThat(foundType).isEqualTo(type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Key"})
    void setDate(final String dateKey) {
        final String value = "1";

        final Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(dateKey, value);

        final BreadcrumbBuilder breadcrumbBuilder = new BreadcrumbBuilder();
        breadcrumbBuilder.setDate(dataMap);

        final Object foundValue = breadcrumbBuilder.build().getData(dateKey);
        assertThat(foundValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Key"})
    void setData(final String dateKey) {
        final String value = "1";

        final BreadcrumbBuilder breadcrumbBuilder = new BreadcrumbBuilder();
        breadcrumbBuilder.setData(dateKey, value);

        final Object foundValue = breadcrumbBuilder.build().getData(dateKey);
        assertThat(foundValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Key"})
    void removeData(final String dateKey) {
        final String value = "1";

        final BreadcrumbBuilder breadcrumbBuilder = new BreadcrumbBuilder();
        breadcrumbBuilder.setData(dateKey, value);

        final Object foundValue = breadcrumbBuilder.build().getData(dateKey);
        assertThat(foundValue).isEqualTo(value);

        breadcrumbBuilder.removeData(dateKey);

        final Object foundValueAfterRemove = breadcrumbBuilder.build().getData(dateKey);
        assertThat(foundValueAfterRemove).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Category"})
    void setCategory(final String category) {
        final BreadcrumbBuilder breadcrumbBuilder = new BreadcrumbBuilder();
        breadcrumbBuilder.setCategory(category);

        final String foundCategory = breadcrumbBuilder.build().getCategory();
        assertThat(foundCategory).isEqualTo(category);
    }

    @ParameterizedTest
    @EnumSource(SentryLevel.class)
    void setLevel(final SentryLevel level) {
        final BreadcrumbBuilder breadcrumbBuilder = new BreadcrumbBuilder();
        breadcrumbBuilder.setLevel(level);

        final SentryLevel foundLevel = breadcrumbBuilder.build().getLevel();
        assertThat(foundLevel).isEqualTo(level);
    }
}