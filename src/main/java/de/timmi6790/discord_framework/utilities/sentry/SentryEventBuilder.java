package de.timmi6790.discord_framework.utilities.sentry;

import io.sentry.Breadcrumb;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SentryEventBuilder {
    private final SentryEvent sentryEvent = new SentryEvent();

    public SentryEvent build() {
        return this.sentryEvent;
    }

    public SentryEventBuilder setMessage(final String message) {
        final Message sentryMessage = new Message();
        sentryMessage.setMessage(message);

        this.sentryEvent.setMessage(sentryMessage);
        return this;
    }

    public SentryEventBuilder setMessage(final Message message) {
        this.sentryEvent.setMessage(message);
        return this;
    }

    public SentryEventBuilder setServerName(final String serverName) {
        this.sentryEvent.setServerName(serverName);
        return this;
    }

    public SentryEventBuilder setPlatform(final String platform) {
        this.sentryEvent.setPlatform(platform);
        return this;
    }

    public SentryEventBuilder setRelease(final String release) {
        this.sentryEvent.setRelease(release);
        return this;
    }

    public SentryEventBuilder setDist(final String dist) {
        this.sentryEvent.setDist(dist);
        return this;
    }

    public SentryEventBuilder setLogger(final String logger) {
        this.sentryEvent.setLogger(logger);
        return this;
    }

    public SentryEventBuilder setThreads(final List<SentryThread> threads) {
        this.sentryEvent.setThreads(threads);
        return this;
    }

    public SentryEventBuilder setExceptions(final List<SentryException> exception) {
        this.sentryEvent.setExceptions(exception);
        return this;
    }

    public SentryEventBuilder setEventId(final SentryId eventId) {
        this.sentryEvent.setEventId(eventId);
        return this;
    }

    public SentryEventBuilder setThrowable(final @Nullable Throwable throwable) {
        this.sentryEvent.setThrowable(throwable);
        return this;
    }

    public SentryEventBuilder setLevel(final SentryLevel level) {
        this.sentryEvent.setLevel(level);
        return this;
    }

    public SentryEventBuilder setTransaction(final String transaction) {
        this.sentryEvent.setTransaction(transaction);
        return this;
    }

    public SentryEventBuilder setEnvironment(final String environment) {
        this.sentryEvent.setEnvironment(environment);
        return this;
    }

    public SentryEventBuilder setUser(final User user) {
        this.sentryEvent.setUser(user);
        return this;
    }

    public SentryEventBuilder setRequest(final Request request) {
        this.sentryEvent.setRequest(request);
        return this;
    }

    public SentryEventBuilder setSdk(final SdkVersion sdk) {
        this.sentryEvent.setSdk(sdk);
        return this;
    }

    public SentryEventBuilder setFingerprints(final List<String> fingerprint) {
        this.sentryEvent.setFingerprints(fingerprint);
        return this;
    }

    public SentryEventBuilder setBreadcrumbs(final List<Breadcrumb> breadcrumbs) {
        this.sentryEvent.setBreadcrumbs(breadcrumbs);
        return this;
    }

    public SentryEventBuilder addBreadcrumb(final Breadcrumb breadcrumb) {
        this.sentryEvent.addBreadcrumb(breadcrumb);
        return this;
    }

    public SentryEventBuilder addBreadcrumb(final @Nullable String message) {
        this.sentryEvent.addBreadcrumb(message);
        return this;
    }

    public SentryEventBuilder setTags(final Map<String, String> tags) {
        this.sentryEvent.setTags(tags);
        return this;
    }

    public SentryEventBuilder setTag(final String key, final String value) {
        this.sentryEvent.setTag(key, value);
        return this;
    }

    public SentryEventBuilder setExtras(final Map<String, Object> extra) {
        this.sentryEvent.setExtras(extra);
        return this;
    }

    public SentryEventBuilder setExtra(final String key, final Object value) {
        this.sentryEvent.setExtra(key, value);
        return this;
    }

    public SentryEventBuilder acceptUnknownProperties(final Map<String, Object> unknown) {
        this.sentryEvent.acceptUnknownProperties(unknown);
        return this;
    }

    public SentryEventBuilder setModules(final Map<String, String> modules) {
        this.sentryEvent.setModules(modules);
        return this;
    }

    public SentryEventBuilder setModule(final String key, final String value) {
        this.sentryEvent.setModule(key, value);
        return this;
    }

    public SentryEventBuilder setDebugMeta(final DebugMeta debugMeta) {
        this.sentryEvent.setDebugMeta(debugMeta);
        return this;
    }
}
