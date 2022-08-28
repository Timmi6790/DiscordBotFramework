package de.timmi6790.discord_framework.module.modules.event;

import com.google.common.collect.SetMultimap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventModuleTest {
    private EventModule generateEventModule() {
        return new EventModule();
    }

    @Test
    void addEventListener() {
        final EventModule eventModule = this.generateEventModule();
        final TestEventListener testEventListener = new TestEventListener();

        final boolean registered = eventModule.addEventListener(testEventListener);
        assertThat(registered).isTrue();
        assertThat(eventModule.getEventListeners()).hasSize(1);

        final SetMultimap<EventPriority, EventObject> listeners = eventModule.getEventListeners().get(GenericEvent.class);
        assertThat(listeners.values()).hasSize(2);
    }

    @Test
    void addEventListeners() {
        final EventModule eventModule = this.generateEventModule();
        final TestEventListener testEventListener = new TestEventListener();
        final TestEventListener2 testEventListener2 = new TestEventListener2();

        eventModule.addEventListeners(
                testEventListener,
                testEventListener2
        );
        assertThat(eventModule.getEventListeners()).hasSize(1);

        final SetMultimap<EventPriority, EventObject> listeners = eventModule.getEventListeners().get(GenericEvent.class);
        assertThat(listeners.values()).hasSize(4);
    }

    @Test
    void removeEventListener() {
        final EventModule eventModule = this.generateEventModule();
        final TestEventListener testEventListener = new TestEventListener();
        final TestEventListener2 testEventListener2 = new TestEventListener2();

        eventModule.addEventListeners(
                testEventListener,
                testEventListener2
        );
        eventModule.removeEventListener(testEventListener2);

        final SetMultimap<EventPriority, EventObject> listeners = eventModule.getEventListeners().get(GenericEvent.class);
        assertThat(listeners.values()).hasSize(2);
    }

    @Test
    void clearEventListener() {
        final EventModule eventModule = this.generateEventModule();
        final TestEventListener testEventListener = new TestEventListener();
        final TestEventListener2 testEventListener2 = new TestEventListener2();

        eventModule.addEventListeners(
                testEventListener,
                testEventListener2
        );

        eventModule.clearEventListener();
        assertThat(eventModule.getEventListeners()).isEmpty();
    }

    @Test
    void executeEvent_async() {
        final EventModule eventModule = this.generateEventModule();
        final EventCallListener listener = spy(new EventCallListener());

        eventModule.addEventListener(listener);
        eventModule.executeEvent(new TestEvent());

        verify(listener, timeout(1_000).times(1)).listener(any());
    }

    @Test
    void executeEvent_sync() {
        final EventModule eventModule = this.generateEventModule();
        final EventCallListener listener = spy(new EventCallListener());

        eventModule.addEventListener(listener);
        eventModule.executeEvent(new CancelableEvent());

        verify(listener).cancelListener(any());
    }

    @Test
    void registeredEvent_invalid_parameter_amount() {
        final InvalidParameterCountTestListener event = new InvalidParameterCountTestListener();
        final EventModule eventModule = this.generateEventModule();

        assertThat(eventModule.addEventListener(event)).isFalse();
    }

    @Test
    void registeredEvent_invalid_type_parameter() {
        final InvalidTypeParameterListener event = new InvalidTypeParameterListener();
        final EventModule eventModule = this.generateEventModule();

        assertThat(eventModule.addEventListener(event)).isFalse();
    }

    private static class TestEventListener {
        public void notListener1() {

        }

        @SubscribeEvent
        public void listener1(final GenericEvent event) {

        }

        public void notListener2() {

        }

        @SubscribeEvent
        public void listener2(final GenericEvent event) {

        }
    }

    private static class TestEventListener2 {
        @SubscribeEvent
        public void listener10(final GenericEvent event) {

        }

        @SubscribeEvent
        public void listener20(final GenericEvent event) {

        }
    }

    private static class EventCallListener {
        @SubscribeEvent
        public void listener(final TestEvent event) {

        }

        @SubscribeEvent
        public void cancelListener(final CancelableEvent event) {

        }
    }

    private static class InvalidParameterCountTestListener {
        @SubscribeEvent
        public void listener() {

        }
    }

    private static class InvalidTypeParameterListener {
        @SubscribeEvent
        public void listener(final String string) {

        }
    }

    private static class TestEvent implements GenericEvent {
        @NotNull
        @Override
        public JDA getJDA() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getResponseNumber() {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public DataObject getRawData() {
            return null;
        }
    }

    private static class CancelableEvent implements GenericEvent, Cancelable {
        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void setCancelled(final boolean cancelled) {

        }

        @NotNull
        @Override
        public JDA getJDA() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getResponseNumber() {
            return 0;
        }

        @Nullable
        @Override
        public DataObject getRawData() {
            return null;
        }
    }
}