package pegasus.eventbus.client;

import static org.junit.Assert.*;

import org.junit.Test;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.Subscription;

public class SubscriptionTest {

    static final TestEventHandler    nonNullEventHandler    = new TestEventHandler();
    static final TestEnvelopeHandler nonNullEnvelopeHandler = new TestEnvelopeHandler();

    @Test(expected = IllegalArgumentException.class)
    public void creatingSubscriptionWithANullEventHandlerShouldThrow() {
        new Subscription((EventHandler<?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creatingSubscriptionWithANullEventHandlerWithAQueueNameShouldThrow() {
        new Subscription((EventHandler<?>) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void creatingSubscriptionWithZeroLengthQueueNameShouldThrow() {
        new Subscription(nonNullEventHandler, "");
    }

    @Test
    public void creatingSubscriptionWithNullEventQueueNameShouldntThrow() {
        new Subscription(nonNullEventHandler, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creatingSubscriptionWithANullEnvelopeHandlerShouldThrow() {
        new Subscription((EnvelopeHandler) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creatingSubscriptionWithZeroLengthEventSetShouldThrow() {
        new Subscription(nonNullEnvelopeHandler, "");
    }

    @Test
    public void creatingSubscriptionWithNullEnvelopeQueueNameShouldntThrow() {
        new Subscription(nonNullEnvelopeHandler, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void settingQueueNameToZeroLengthStringShouldThrow() {
        Subscription sub = new Subscription(nonNullEventHandler);
        sub.setQueueName("");
    }

    @Test
    public void creatingSubscriptionWithoutAQueueNameShouldInitializeIsDurableToFalse() {
        Subscription sub = new Subscription(nonNullEventHandler);
        assertFalse(sub.getIsDurable());
    }

    @Test
    public void creatingSubscriptionWithAQueueNameShouldInitializeIsDurableToTrue() {
        Subscription sub = new Subscription(nonNullEventHandler, "AName");
        assertTrue(sub.getIsDurable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void settingIsDurableToTrueWithoutAQueueNameShouldThrow() {
        Subscription sub = new Subscription(nonNullEventHandler);
        assertNull(sub.getQueueName());
        sub.setIsDurable(true);
    }

    @Test
    public void settingIsDurableToTrueOrFalseWithAQueueNameShouldSucceed() {
        Subscription sub = new Subscription(nonNullEventHandler);
        sub.setQueueName("AName");
        assertFalse(sub.getIsDurable());
        sub.setIsDurable(true);
        assertTrue(sub.getIsDurable());
        sub.setIsDurable(false);
        assertFalse(sub.getIsDurable());
    }

    @Test
    public void settingQueueNameToNullShouldForceIsDurableToFalse() {
        Subscription sub = new Subscription(nonNullEventHandler, "AName");
        assertTrue(sub.getIsDurable());
        sub.setQueueName(null);
        assertFalse(sub.getIsDurable());
    }

    private static class TestEventHandler implements EventHandler<Object> {

        @Override
        public Class<? extends Object>[] getHandledEventTypes() {
            return null;
        }

        @Override
        public EventResult handleEvent(Object event) {
            return null;
        }
    }

    private static class TestEnvelopeHandler implements EnvelopeHandler {

        @Override
        public EventResult handleEnvelope(Envelope envelope) {
            return null;
        }

        @Override
        public String getEventSetName() {
            return null;
        }
    }
}
