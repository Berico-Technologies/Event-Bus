package gov.ment.eventbus.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import gov.ment.eventbus.client.Envelope;
import gov.ment.eventbus.client.EnvelopeHandler;
import gov.ment.eventbus.client.EventHandler;
import gov.ment.eventbus.client.EventResult;
import gov.ment.eventbus.client.Subscription;

import org.junit.Test;

public class SubscriptionTest {

  private static class TestEnvelopeHandler implements EnvelopeHandler {

    @Override
    public String getEventSetName() {
      return null;
    }

    @Override
    public EventResult handleEnvelope(Envelope envelope) {
      return null;
    }
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

  static final TestEventHandler nonNullEventHandler = new TestEventHandler();

  static final TestEnvelopeHandler nonNullEnvelopeHandler = new TestEnvelopeHandler();

  @Test(expected = IllegalArgumentException.class)
  public void creatingSubscriptionWithANullEnvelopeHandlerShouldThrow() {
    new Subscription((EnvelopeHandler) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void creatingSubscriptionWithANullEventHandlerShouldThrow() {
    new Subscription((EventHandler<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void creatingSubscriptionWithANullEventHandlerWithAQueueNameShouldThrow() {
    new Subscription((EventHandler<?>) null, "name");
  }

  @Test
  public void creatingSubscriptionWithAQueueNameShouldInitializeIsDurableToTrue() {
    Subscription sub = new Subscription(nonNullEventHandler, "AName");
    assertTrue(sub.getIsDurable());
  }

  @Test
  public void creatingSubscriptionWithNullEnvelopeQueueNameShouldntThrow() {
    new Subscription(nonNullEnvelopeHandler, null);
  }

  @Test
  public void creatingSubscriptionWithNullEventQueueNameShouldntThrow() {
    new Subscription(nonNullEventHandler, null);
  }

  @Test
  public void creatingSubscriptionWithoutAQueueNameShouldInitializeIsDurableToFalse() {
    Subscription sub = new Subscription(nonNullEventHandler);
    assertFalse(sub.getIsDurable());
  }

  @Test(expected = IllegalArgumentException.class)
  public void creatingSubscriptionWithZeroLengthEventSetShouldThrow() {
    new Subscription(nonNullEnvelopeHandler, "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void creatingSubscriptionWithZeroLengthQueueNameShouldThrow() {
    new Subscription(nonNullEventHandler, "");
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

  @Test(expected = IllegalArgumentException.class)
  public void settingIsDurableToTrueWithoutAQueueNameShouldThrow() {
    Subscription sub = new Subscription(nonNullEventHandler);
    assertNull(sub.getQueueName());
    sub.setIsDurable(true);
  }

  @Test
  public void settingQueueNameToNullShouldForceIsDurableToFalse() {
    Subscription sub = new Subscription(nonNullEventHandler, "AName");
    assertTrue(sub.getIsDurable());
    sub.setQueueName(null);
    assertFalse(sub.getIsDurable());
  }

  @Test(expected = IllegalArgumentException.class)
  public void settingQueueNameToZeroLengthStringShouldThrow() {
    Subscription sub = new Subscription(nonNullEventHandler);
    sub.setQueueName("");
  }
}
