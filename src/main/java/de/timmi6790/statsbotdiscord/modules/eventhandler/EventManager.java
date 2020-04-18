package de.timmi6790.statsbotdiscord.modules.eventhandler;

import de.timmi6790.statsbotdiscord.StatsBot;
import net.dv8tion.jda.api.events.Event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private final Map<Class<Event>, List<EventObject>> eventListeners = new ConcurrentHashMap<>();

    public EventManager() {
        StatsBot.getDiscord().addEventListener(new DiscordEventListener());
    }

    public void addEventListener(final Object listener) {
        for (final Method method : listener.getClass().getMethods()) {
            final Annotation[] annotations = method.getDeclaredAnnotations();

            boolean found = false;
            for (final Annotation annotation : annotations) {
                if (annotation instanceof SubscribeEvent) {
                    found = true;
                    break;
                }
            }

            if (!found || method.getParameterCount() != 1) {
                continue;
            }

            final Class<?> parameter = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(parameter)) {
                continue;
            }

            if (!this.eventListeners.containsKey(parameter)) {
                this.eventListeners.put((Class<Event>) parameter, new CopyOnWriteArrayList<>());
            }

            this.eventListeners.get(parameter).add(new EventObject(listener, method));
        }
    }

    public void addEventListeners(final Object... listeners) {
        for (final Object clazz : listeners) {
            this.addEventListener(clazz);
        }
    }

    public void removeEventListener(final Object listener) {
        for (final List<EventObject> listeners : this.eventListeners.values()) {
            final List<EventObject> removeList = new ArrayList<>();

            for (final EventObject pair : listeners) {
                if (pair.getObject().equals(listener)) {
                    removeList.add(pair);
                }
            }

            for (final EventObject remove : removeList) {
                listeners.remove(remove);
            }
        }
    }

    public void clearEventListener() {
        this.eventListeners.clear();
    }

    public void callEvent(final Event event) {
        if (!this.eventListeners.containsKey(event.getClass())) {
            return;
        }

        for (final EventObject listener : this.eventListeners.get(event.getClass())) {
            try {
                listener.getMethod().invoke(listener.getObject(), event);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class EventObject {
        private final Object object;
        private final Method method;

        private EventObject(final Object object, final Method method) {
            this.object = object;
            this.method = method;
        }

        public Object getObject() {
            return this.object;
        }

        public Method getMethod() {
            return this.method;
        }
    }
}
