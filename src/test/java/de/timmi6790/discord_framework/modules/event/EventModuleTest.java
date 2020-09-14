package de.timmi6790.discord_framework.modules.event;

import com.google.common.collect.SetMultimap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class EventModuleTest {
    private EventModule getEventModule() {
        return new EventModule();
    }

    @Test
    void addEventListener() {
        final EventModule eventModule = this.getEventModule();
        final TestEventListener testEventListener = new TestEventListener();


        final boolean registered = eventModule.addEventListener(testEventListener);
        assertThat(registered).isTrue();
        assertThat(eventModule.getEventListeners()).hasSize(1);

        final SetMultimap<EventPriority, EventObject> listeners = eventModule.getEventListeners().get(GenericEvent.class);
        assertThat(listeners.values()).hasSize(2);
    }

    @Test
    void addEventListeners() {
        final EventModule eventModule = this.getEventModule();
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
        final EventModule eventModule = this.getEventModule();
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
        final EventModule eventModule = this.getEventModule();
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
    void executeEvent() throws InterruptedException {
        final EventModule eventModule = this.getEventModule();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final TestEventListerCountDownLatch testEventListerCountDownLatch = new TestEventListerCountDownLatch(countDownLatch);

        eventModule.addEventListener(testEventListerCountDownLatch);
        eventModule.executeEvent(new TestEvent());

        final boolean waited = countDownLatch.await(2, TimeUnit.SECONDS);
        assertThat(waited).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
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
        public void notListener10() {

        }

        @SubscribeEvent
        public void listener10(final GenericEvent event) {

        }

        public void notListener20() {

        }

        @SubscribeEvent
        public void listener20(final GenericEvent event) {

        }
    }

    private static class TestEventListerCountDownLatch {
        private final CountDownLatch countDownLatch;

        private TestEventListerCountDownLatch(final CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @SubscribeEvent
        public void listener(final TestEvent event) {
            this.countDownLatch.countDown();
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
    }
}