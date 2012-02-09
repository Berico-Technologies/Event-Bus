package pegasus.eventbus.amqp;

import static org.junit.Assert.*;

import org.junit.Test;

import pegasus.eventbus.testsupport.TestResponseEvent;

public class AmqpEventManager_NonBlockingRPCPublishEnvelopeTest extends
		AmqpEventManager_PublishEnvelopeTestBase {

	TestEventHandler handler = new TestEventHandler(TestResponseEvent.class);
	
	@Override
	protected void publish() {
		manager.getResponseTo(sendEvent, handler);
	}

	@Test 
	public void thePublishedEnvelopeShouldSpecifyTheNameOfTheCreatedQueueAsItsReplyToValue(){
		assertEquals(getCreatedQueueName(), publishedEnvelope.getReplyTo());
	}
}