package de.timmi6790.discord_framework.modules.event;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import de.timmi6790.commons.utilities.ReflectionUtilities;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.utilities.sentry.BreadcrumbBuilder;
import de.timmi6790.discord_framework.utilities.sentry.SentryEventBuilder;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EqualsAndHashCode(callSuper = true)
public class EventModule extends AbstractModule {
    private static final String EVENT = "Event";

    @Getter(value = AccessLevel.PROTECTED)
    private final Map<Class<GenericEvent>, SetMultimap<EventPriority, EventObject>> eventListeners = new HashMap<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final TaggedLogger logger;

    public EventModule() {
        super(EVENT);

        this.logger = Logger.tag("DiscordFramework");
    }

    @Override
    public void onEnable() {
        this.getDiscord().addEventListener(new DiscordEventListener(this));
    }

    private void handleEventException(final Exception exception, final GenericEvent event, final EventObject listener) {
        this.logger.error(exception);

        // Sentry error
        Sentry.captureEvent(new SentryEventBuilder()
                .addBreadcrumb(new BreadcrumbBuilder()
                        .setCategory(EVENT)
                        .setData("Class", event.getClass().toString())
                        .setData("Listener", listener.getMethod().getName())
                        .build())
                .setLevel(SentryLevel.ERROR)
                .setMessage(EVENT)
                .setLogger(EventObject.class.getName())
                .setThrowable(exception)
                .build());
    }

    public boolean addEventListener(final Object listener) {
        boolean registeredListener = false;
        for (final Method method : listener.getClass().getMethods()) {
            final Optional<SubscribeEvent> annotationOpt = ReflectionUtilities.getAnnotation(method, SubscribeEvent.class);
            if (annotationOpt.isPresent()) {
                final SubscribeEvent annotation = annotationOpt.get();

                if (method.getParameterCount() != 1) {
                    this.logger.warn(
                            "{}.{} has the SubscribeEvent Annotation, but has an incorrect parameter count of {}.",
                            listener.getClass(),
                            method.getName(),
                            method.getParameterCount()
                    );
                    continue;
                }

                final Class<?> parameter = method.getParameterTypes()[0];
                if (!GenericEvent.class.isAssignableFrom(parameter)) {
                    this.logger.warn(
                            "{}.{} has the SubscribeEvent Annotation, but the parameter is not extending GenericEvent",
                            listener.getClass(),
                            method.getName());
                    continue;
                }

                registeredListener = true;
                this.eventListeners.computeIfAbsent(
                        (Class<GenericEvent>) parameter,
                        key -> MultimapBuilder.enumKeys(EventPriority.class).hashSetValues().build()
                ).put(
                        annotation.priority(),
                        new EventObject(listener, method, annotation.ignoreCanceled())
                );

                this.logger.info(
                        "Added {}.{} as new event listener for {}.",
                        listener.getClass(),
                        method.getName(),
                        parameter.getName()
                );
            }
        }

        return registeredListener;
    }

    public void addEventListeners(final Object... listeners) {
        for (final Object listener : listeners) {
            this.addEventListener(listener);
        }
    }

    public void removeEventListener(final Object listener) {
        for (final SetMultimap<EventPriority, EventObject> value : this.eventListeners.values()) {
            final Iterator<EventObject> valueIterator = value.values().iterator();
            while (valueIterator.hasNext()) {
                final EventObject eventObject = valueIterator.next();
                if (eventObject.getObject().equals(listener)) {
                    for (final Method method : listener.getClass().getMethods()) {
                        if (eventObject.getMethod().equals(method)) {
                            valueIterator.remove();
                        }
                    }
                }
            }
        }
    }

    public void clearEventListener() {
        this.eventListeners.clear();
    }

    public void executeEvent(final GenericEvent event) {
        if (!this.eventListeners.containsKey(event.getClass())) {
            return;
        }

        final SetMultimap<EventPriority, EventObject> entry = this.eventListeners.get(event.getClass());
        final boolean canCancel = Cancelable.class.isAssignableFrom(event.getClass());
        for (final EventObject listener : entry.values()) {
            if (canCancel && !listener.isIgnoreCanceled() && ((Cancelable) event).isCancelled()) {
                continue;
            }

            // If there is no way to cancel the event, we can run it in multiple threads
            if (canCancel) {
                try {
                    listener.getMethod().invoke(listener.getObject(), event);
                } catch (final Exception e) {
                    this.handleEventException(e, event, listener);

                }
            } else {
                this.executorService.execute(() -> {
                    try {
                        listener.getMethod().invoke(listener.getObject(), event);
                    } catch (final Exception e) {
                        this.handleEventException(e, event, listener);
                    }
                });
            }
        }
    }
}
