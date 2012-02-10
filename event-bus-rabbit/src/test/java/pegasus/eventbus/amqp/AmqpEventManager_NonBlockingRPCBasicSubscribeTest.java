package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.SubscriptionToken;

public class AmqpEventManager_NonBlockingRPCBasicSubscribeTest extends
		AmqpEventManager_BasicSubscribeTestBase {

	@Override
	protected SubscriptionToken subscribe() {
		return manager.getResponseTo(sendEvent, handler);
	}

	@Override
	protected String getRouteSuffix() {
		//Routing keys for RPC subscriptions should include the reply-to queueName as the final segment.
		return getCreatedQueueName();
	}
	
	private boolean hasStartedListening;
	private boolean hadStartedListeningPriorToEnvelopePublication;
	@Test
	@Ignore("Needs update to conform to use of basicConsume.")
	public void theEnvelopeShouldNotBePublishedBeforeTheResponseHandlerIsPolling(){
		doAnswer(new Answer<Object>() {
		     public Object answer(InvocationOnMock invocation) {
		    	 log.debug("getNextMessage called for queue;" + invocation.getArguments()[0].toString());
		         hasStartedListening = true;
		         return null;
		     }
		 }).when(messageBus).getNextMessageFrom(anyString());
		doAnswer(new Answer<Object>() {
		     public Object answer(InvocationOnMock invocation) {
		    	 log.debug("publish called.");
		         hadStartedListeningPriorToEnvelopePublication = hasStartedListening;
		         return null;
		     }
		 }).when(messageBus).publish(any(RoutingInfo.class), any(Envelope.class));
		
		subscribe();
		
		assertTrue(hadStartedListeningPriorToEnvelopePublication);
	}
}