package de.timmi6790.discord_framework.modules.event;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.datatypes.builders.MapBuilder;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.utilities.ReflectionUtilities;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EqualsAndHashCode(callSuper = true)
public class EventModule extends AbstractModule {
    @Getter(value = AccessLevel.PROTECTED)
    private final Map<Class<GenericEvent>, SetMultimap<EventPriority, EventObject>> eventListeners = new HashMap<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public EventModule() {
        super("Event");
    }

    @Override
    public void onEnable() {
        this.getDiscord().addEventListener(new DiscordEventListener());
    }

    private void handleEventException(final Exception exception, final GenericEvent event, final EventObject listener) {
        DiscordBot.getLogger().error(exception);

        // Sentry error
        final Map<String, String> data = MapBuilder.<String, String>ofHashMap(2)
                .put("Class", event.getClass().toString())
                .put("Listener", listener.getMethod().getName())
                .build();

        final Breadcrumb breadcrumb = new BreadcrumbBuilder()
                .setCategory("Event")
                .setData(data)
                .build();

        final EventBuilder eventBuilder = new EventBuilder()
                .withMessage("Event Exception")
                .withLevel(Event.Level.ERROR)
                .withBreadcrumbs(Collections.singletonList(breadcrumb))
                .withLogger(EventObject.class.getName())
                .withSentryInterface(new ExceptionInterface(exception));

        this.getSentry().sendEvent(eventBuilder);
    }

    public boolean addEventListener(final Object listener) {
        final boolean[] registeredListener = new boolean[]{false};
        for (final Method method : listener.getClass().getMethods()) {
            ReflectionUtilities.getAnnotation(method, SubscribeEvent.class).ifPresent(annotation -> {
                        if (method.getParameterCount() != 1) {
                            DiscordBot.getLogger().warn("{}.{} has the SubscribeEvent Annotation, but has an incorrect parameter count of {}.",
                                    listener.getClass(), method.getName(), method.getParameterCount());
                            return;
                        }

                        final Class<?> parameter = method.getParameterTypes()[0];
                        if (!GenericEvent.class.isAssignableFrom(parameter)) {
                            DiscordBot.getLogger().warn("{}.{} has the SubscribeEvent Annotation, but the parameter is not extending GenericEvent",
                                    listener.getClass(), method.getName());
                            return;
                        }

                        registeredListener[0] = true;
                        this.eventListeners.computeIfAbsent(
                                (Class<GenericEvent>) parameter,
                                key -> MultimapBuilder.enumKeys(EventPriority.class).hashSetValues().build()
                        ).put(annotation.priority(), new EventObject(listener, method, annotation.ignoreCanceled()));
                        DiscordBot.getLogger().info("Added {}.{} as new event listener for {}.", listener.getClass(), method.getName(), parameter.getName());
                    }
            );
        }

        return registeredListener[0];
    }

    public void addEventListeners(final Object... listeners) {
        Arrays.stream(listeners).forEach(this::addEventListener);
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
