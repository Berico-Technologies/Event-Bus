package gov.ment.eventbus.amqp;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import gov.ment.eventbus.testsupport.TestResponseEvent;

import org.junit.Test;

public class AmqpEventManager_BlockingRPCPublishEnvelopeTest extends
        AmqpEventManager_PublishEnvelopeTestBase {

  @Override
  protected void publish() {
    try {
      @SuppressWarnings({ "unchecked", "unused" })
      TestResponseEvent event = manager.getResponseTo(sendEvent, 100, TestResponseEvent.class);
    } catch (TimeoutException e) {
      // This is okay since we did not stub a response;
    } catch (InterruptedException e) {
      fail("Thread was interrupted.");
    }
  }

  @Test
  public void thePublishedEnvelopeShouldSpecifyTheNameOfTheCreatedQueueAsItsReplyToValue() {
    assertEquals(getCreatedQueueName(), publishedEnvelope.getReplyTo());
  }
}
