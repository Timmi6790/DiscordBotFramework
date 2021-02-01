package de.timmi6790.discord_framework.utilities.sentry;

import io.sentry.Breadcrumb;
import io.sentry.SentryLevel;
import io.sentry.protocol.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SentryEventBuilderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "A really really really really really long message"})
    void setMessage_string(final String message) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setMessage(message);

        final String foundMessage = sentryEventBuilder.build().getMessage().getMessage();
        assertThat(foundMessage).isEqualTo(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "A really really really really really long message"})
    void setMessage(final String message) {
        final Message messageObject = new Message();
        messageObject.setMessage(message);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setMessage(messageObject);

        final String foundMessage = sentryEventBuilder.build().getMessage().getMessage();
        assertThat(foundMessage).isEqualTo(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Server", "Server2", "Server-21_A Debian"})
    void setServerName(final String serverName) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setServerName(serverName);

        final String foundServerName = sentryEventBuilder.build().getServerName();
        assertThat(foundServerName).isEqualTo(serverName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Server", "Server2", "Server-21_A Debian"})
    void setPlatform(final String platformName) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setPlatform(platformName);

        final String foundPlatformName = sentryEventBuilder.build().getPlatform();
        assertThat(foundPlatformName).isEqualTo(platformName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0.0", "1.0.1a"})
    void setRelease(final String release) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setRelease(release);

        final String foundRelease = sentryEventBuilder.build().getRelease();
        assertThat(foundRelease).isEqualTo(release);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0.0", "1.0.1a"})
    void setDist(final String distribution) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setDist(distribution);

        final String foundDist = sentryEventBuilder.build().getDist();
        assertThat(foundDist).isEqualTo(distribution);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0.0", "1.0.1a"})
    void setLogger(final String logger) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setLogger(logger);

        final String foundLogger = sentryEventBuilder.build().getLogger();
        assertThat(foundLogger).isEqualTo(logger);
    }

    @Test
    void setThreads() {
        final List<SentryThread> threads = new ArrayList<>();

        threads.add(new SentryThread());

        final SentryThread crashedSentryThread = new SentryThread();
        crashedSentryThread.setCrashed(true);
        threads.add(crashedSentryThread);

        final SentryThread namedSentryThread = new SentryThread();
        namedSentryThread.setName("Test");
        threads.add(namedSentryThread);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setThreads(threads);

        final List<SentryThread> foundThreads = sentryEventBuilder.build().getThreads();
        assertThat(foundThreads).containsExactly(threads.toArray(new SentryThread[0]));
    }

    @Test
    void setExceptions() {
        final List<SentryException> sentryExceptions = new ArrayList<>();

        sentryExceptions.add(new SentryException());

        final SentryException sentryException = new SentryException();
        sentryException.setModule("Test");
        sentryExceptions.add(sentryException);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setExceptions(sentryExceptions);

        final List<SentryException> foundSentryExceptions = sentryEventBuilder.build().getExceptions();
        assertThat(foundSentryExceptions).containsExactly(sentryExceptions.toArray(new SentryException[0]));
    }

    @Test
    void setEventId() {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();

        final SentryId sentryId = new SentryId(UUID.randomUUID());
        sentryEventBuilder.setEventId(sentryId);

        final SentryId foundSentryId = sentryEventBuilder.build().getEventId();
        assertThat(foundSentryId).isEqualTo(sentryId);
    }

    @Test
    void setThrowable() {
        final RuntimeException runtimeException = new RuntimeException("Test");

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setThrowable(runtimeException);

        final Throwable foundThrowable = sentryEventBuilder.build().getThrowable();
        assertThat(foundThrowable).isEqualTo(runtimeException);
    }

    @ParameterizedTest
    @EnumSource(SentryLevel.class)
    void setLevel(final SentryLevel level) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setLevel(level);

        final SentryLevel foundLevel = sentryEventBuilder.build().getLevel();
        assertThat(foundLevel).isEqualTo(level);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Transaction"})
    void setTransaction(final String transaction) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setTransaction(transaction);

        final String foundTransaction = sentryEventBuilder.build().getTransaction();
        assertThat(foundTransaction).isEqualTo(transaction);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Environment"})
    void setEnvironment(final String environment) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setEnvironment(environment);

        final String foundEnvironment = sentryEventBuilder.build().getEnvironment();
        assertThat(foundEnvironment).isEqualTo(environment);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test UserId"})
    void setUser(final String userId) {
        final User user = new User();
        user.setId(userId);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setUser(user);

        final User foundUser = sentryEventBuilder.build().getUser();
        assertThat(foundUser.getId()).isEqualTo(userId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Data"})
    void setRequest(final String data) {
        final Request request = new Request();
        request.setData(data);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setRequest(request);

        final Request foundRequest = sentryEventBuilder.build().getRequest();
        assertThat(foundRequest).isNotNull();
        assertThat(foundRequest.getData()).isEqualTo(data);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Version"})
    void setSdk(final String sdkVersionName) {
        final SdkVersion sdkVersion = new SdkVersion();
        sdkVersion.setName(sdkVersionName);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setSdk(sdkVersion);

        final SdkVersion foundSdkVersion = sentryEventBuilder.build().getSdk();
        assertThat(foundSdkVersion).isNotNull();
        assertThat(foundSdkVersion.getName()).isEqualTo(sdkVersionName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test FingerSprint"})
    void setFingerprints(final String fingerprint) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setFingerprints(Collections.singletonList(fingerprint));

        final List<String> foundFingerprints = sentryEventBuilder.build().getFingerprints();
        assertThat(foundFingerprints).containsExactly(fingerprint);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setBreadcrumbs(final String breadcrumbMessage) {
        final Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setMessage(breadcrumbMessage);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setBreadcrumbs(Collections.singletonList(breadcrumb));

        final List<Breadcrumb> foundBreadcrumbs = sentryEventBuilder.build().getBreadcrumbs();
        assertThat(foundBreadcrumbs).containsExactly(breadcrumb);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void addBreadcrumb(final String breadcrumbMessage) {
        final Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setMessage(breadcrumbMessage);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.addBreadcrumb(breadcrumb);

        final List<Breadcrumb> foundBreadcrumbs = sentryEventBuilder.build().getBreadcrumbs();
        assertThat(foundBreadcrumbs).containsExactly(breadcrumb);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void addBreadcrumb_string(final String breadcrumbMessage) {
        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.addBreadcrumb(breadcrumbMessage);

        final List<Breadcrumb> foundBreadcrumbs = sentryEventBuilder.build().getBreadcrumbs();
        assertThat(foundBreadcrumbs).hasSize(1);
        assertThat(foundBreadcrumbs.get(0).getMessage()).isEqualTo(breadcrumbMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setTags(final String tagKey) {
        final String value = "1";
        final Map<String, String> valueMap = new HashMap<>();
        valueMap.put(tagKey, value);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setTags(valueMap);

        final String foundValue = sentryEventBuilder.build().getTag(tagKey);
        assertThat(foundValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setTag(final String tagKey) {
        final String value = "1";

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setTag(tagKey, value);

        final String foundValue = sentryEventBuilder.build().getTag(tagKey);
        assertThat(foundValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setExtras(final String extraKey) {
        final String value = "1";

        final Map<String, Object> extraMap = new HashMap<>();
        extraMap.put(extraKey, value);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setExtras(extraMap);

        final Object foundValue = sentryEventBuilder.build().getExtra(extraKey);
        assertThat(foundValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setExtra(final String extraKey) {
        final String value = "1";

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setExtra(extraKey, value);

        final Object foundValue = sentryEventBuilder.build().getExtra(extraKey);
        assertThat(foundValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void acceptUnknownProperties(final String propertyKey) {
        final Map<String, Object> unknownProperties = new HashMap<>();
        unknownProperties.put(propertyKey, "1");

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.acceptUnknownProperties(unknownProperties);

        final Map<String, Object> foundUnknownProperties = sentryEventBuilder.build().getUnknown();
        assertThat(foundUnknownProperties).containsAllEntriesOf(unknownProperties);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setModules(final String moduleKey) {
        final String value = "1";

        final Map<String, String> moduleMap = new HashMap<>();
        moduleMap.put(moduleKey, value);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setModules(moduleMap);

        final String foundModuleValue = sentryEventBuilder.build().getModule(moduleKey);
        assertThat(foundModuleValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setModule(final String moduleKey) {
        final String value = "1";

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setModule(moduleKey, value);

        final String foundModuleValue = sentryEventBuilder.build().getModule(moduleKey);
        assertThat(foundModuleValue).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Test1", "Long Test Message"})
    void setDebugMeta(final String sdkName) {
        final DebugMeta debugMeta = new DebugMeta();

        final SdkInfo sdkInfo = new SdkInfo();
        sdkInfo.setSdkName(sdkName);
        debugMeta.setSdkInfo(sdkInfo);

        final SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
        sentryEventBuilder.setDebugMeta(debugMeta);

        final DebugMeta foundDebugMeta = sentryEventBuilder.build().getDebugMeta();
        assertThat(foundDebugMeta.getSdkInfo().getSdkName()).isEqualTo(sdkName);
    }
}