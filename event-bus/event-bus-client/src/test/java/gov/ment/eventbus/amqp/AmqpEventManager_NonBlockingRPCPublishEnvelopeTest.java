package gov.ment.eventbus.amqp;

import static org.junit.Assert.*;

import gov.ment.eventbus.testsupport.TestResponseEvent;

import org.junit.Test;

public class AmqpEventManager_NonBlockingRPCPublishEnvelopeTest extends
        AmqpEventManager_PublishEnvelopeTestBase {

  TestEventHandler handler = new TestEventHandler(TestResponseEvent.class);

  @Override
  protected void publish() {
    manager.getResponseTo(sendEvent, handler);
  }

  @Test
  public void thePublishedEnvelopeShouldSpecifyTheNameOfTheCreatedQueueAsItsReplyToValue() {
    assertEquals(getCreatedQueueName(), publishedEnvelope.getReplyTo());
  }
}
