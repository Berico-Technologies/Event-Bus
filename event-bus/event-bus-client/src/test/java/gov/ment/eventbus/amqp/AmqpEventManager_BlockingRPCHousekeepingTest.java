package gov.ment.eventbus.amqp;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;

import gov.ment.eventbus.testsupport.TestResponseEvent;

public class AmqpEventManager_BlockingRPCHousekeepingTest extends
        AmqpEventManager_PublishHousekeepingTestBase {

  @Override
  protected void publish() {
    try {
      @SuppressWarnings({ "unchecked", "unused" })
      TestResponseEvent event = manager.getResponseTo(sendEvent, 1, TestResponseEvent.class);
    } catch (TimeoutException e) {
      // This is okay since we did not stub a response;
    } catch (InterruptedException e) {
      fail("Thread was interrupted.");
    }
  }
}
