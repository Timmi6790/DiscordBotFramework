package de.timmi6790.statsbotdiscord.modules.eventhandler;

import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.Data;
import net.dv8tion.jda.api.events.Event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventManager {
    private final Map<Class<Event>, Map<EventPriority, Set<EventObject>>> eventListeners = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public EventManager() {
        StatsBot.getDiscord().addEventListener(new DiscordEventListener());
    }

    private void callListenerSafe(final EventObject listener, final Event event) {
        try {
            // At the moment all events can't be canceled, that is why we run everything on a different thread
            this.executorService.submit(() -> listener.getMethod().invoke(listener.getObject(), event));
        } catch (final Exception e) {
            e.printStackTrace();
            StatsBot.getSentry().sendException(e);
        }
    }

    public void addEventListener(final Object listener) {
        Arrays.stream(listener.getClass().getMethods())
                .filter(method -> method.getParameterCount() == 1)
                .forEach(method -> {
                    final SubscribeEvent annotation;
                    try {
                        annotation = method.getAnnotation(SubscribeEvent.class);
                    } catch (final NullPointerException ignore) {
                        return;
                    }

                    final Class<?> parameter = method.getParameterTypes()[0];
                    if (Event.class.isAssignableFrom(parameter)) {
                        this.eventListeners.computeIfAbsent((Class<Event>) parameter, key -> new ConcurrentHashMap<>())
                                .computeIfAbsent(annotation.priority(), key -> Collections.synchronizedSet(new HashSet<>()))
                                .add(new EventObject(listener, method));
                    }
                });
    }

    public void addEventListeners(final Object... listeners) {
        Arrays.stream(listeners).forEach(this::addEventListener);
    }

    public void removeEventListener(final Object listener) {
        for (final Map<EventPriority, Set<EventObject>> value : this.eventListeners.values()) {
            final Iterator<Map.Entry<EventPriority, Set<EventObject>>> it = value.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<EventPriority, Set<EventObject>> entry = it.next();
                final boolean removedElement = entry.getValue().removeIf(eventListener -> eventListener.getObject().equals(listener));
                if (removedElement) {
                    if (entry.getValue().isEmpty()) {
                        it.remove();
                    }

                    return;
                }
            }
        }
    }

    public void clearEventListener() {
        this.eventListeners.clear();
    }

    public void executeEvent(final Event event) {
        Optional.ofNullable(this.eventListeners.get(event.getClass()))
                .ifPresent(listeners -> Arrays.stream(EventPriority.values())
                        .map(listeners::get)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .forEach(listener -> this.callListenerSafe(listener, event))
                );
    }

    @Data
    private static class EventObject {
        private final Object object;
        private final Method method;
    }
}
