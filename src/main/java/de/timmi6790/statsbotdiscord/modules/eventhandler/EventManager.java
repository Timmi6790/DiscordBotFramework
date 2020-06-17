package de.timmi6790.statsbotdiscord.modules.eventhandler;

import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.Data;
import net.dv8tion.jda.api.events.Event;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EventManager {
    private final Map<Class<Event>, List<EventObject>> eventListeners = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public EventManager() {
        StatsBot.getDiscord().addEventListener(new DiscordEventListener());
    }

    public void addEventListener(final Object listener) {
        Arrays.stream(listener.getClass().getMethods()).forEach(method -> {
            final boolean annotationFound = Arrays.stream(method.getDeclaredAnnotations()).anyMatch(annotation -> annotation instanceof SubscribeEvent);
            if (!annotationFound || method.getParameterCount() != 1) {
                return;
            }

            final Class<?> parameter = method.getParameterTypes()[0];
            if (Event.class.isAssignableFrom(parameter)) {
                this.eventListeners.computeIfAbsent((Class<Event>) parameter, key -> Collections.synchronizedList(new ArrayList())).add(new EventObject(listener, method));
            }
        });
    }

    public void addEventListeners(final Object... listeners) {
        Arrays.stream(listeners).forEach(this::addEventListener);
    }

    public void removeEventListener(final Object listener) {
        this.eventListeners.values().forEach(listeners ->
                listeners.removeAll(listeners.stream()
                        .filter(pair -> pair.getObject().equals(listener))
                        .collect(Collectors.toList())
                ));
    }

    public void clearEventListener() {
        this.eventListeners.clear();
    }

    public void callEvent(final Event event) {
        if (!this.eventListeners.containsKey(event.getClass())) {
            return;
        }

        this.eventListeners.get(event.getClass()).forEach(listener -> {
            try {
                // At the moment all events can't be canceled, that is why we run everything on a different thread
                this.executorService.submit(() -> listener.getMethod().invoke(listener.getObject(), event));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Data
    private static class EventObject {
        private final Object object;
        private final Method method;
    }
}
