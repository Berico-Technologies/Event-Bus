package gov.ment.eventbus.amqp;

import gov.ment.eventbus.testsupport.TestSendEvent;

import org.junit.*;

import gov.ment.eventbus.client.EventHandler;
import gov.ment.eventbus.client.Subscription;

public class AmqpEventManager_SubscribeValidationTest extends AmqpEventManager_TestBase {

  protected TestSendEvent sendEvent;

  protected byte[] bytesFromSerializer = { 39, 84, 72, 30, 87, 50, 98, 75, 0 };

  private TestEventHandler eventHandler;

  @Before
  @Override
  public void beforeEachTest() {

    super.beforeEachTest();

    eventHandler = new TestEventHandler(TestSendEvent.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void subscribingWithNullQueueNameShouldThrow() {
    manager.subscribe(eventHandler, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void subscribingWithZeroLengthQueueNameShouldThrow() {
    manager.subscribe(eventHandler, "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void subscribingWithNullEventHanlderShouldThrow() {
    manager.subscribe((EventHandler<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void subscribingWithAQueueNameAndNullEventHanlderShouldThrow() {
    manager.subscribe((EventHandler<?>) null, "queueName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void subscribingWithNullSubcriptionShouldThrow() {
    manager.subscribe((Subscription) null);
  }
}
