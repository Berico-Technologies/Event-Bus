package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.SubscriptionToken;
import pegasus.eventbus.testsupport.TestResponseEvent;
import pegasus.eventbus.testsupport.TestSendEvent;
import pegasus.eventbus.testsupport.TestSendEvent2;

public class AmqpEventManager_BlockingRPCBasicSubscribeTest extends
		AmqpEventManager_BasicSubscribeTestBase {

	private boolean delegateFirstSubscribeCallToBase;
	
	@Override
	protected SubscriptionToken subscribe() {
		if(delegateFirstSubscribeCallToBase){
			delegateFirstSubscribeCallToBase = false;
			return super.subscribe();
		}
		
		try {
			@SuppressWarnings({ "unchecked", "unused" })
			Object event = manager.getResponseTo(sendEvent, 100, TestSendEvent.class, TestSendEvent2.class, TestResponseEvent.class);
		} catch (TimeoutException e) {
			//This is okay since we did not stub a response;
		} catch (InterruptedException e) {			
			fail("Thread was interrupted.");
		}
		return null;
	}

	@Override
	protected void unsubscribe(SubscriptionToken token) {
		//Do nothing in this case.  The manager should have already unsubscribed before returning 
		//form getReponseTo.
	}

	@Override
	protected String getRouteSuffix() {
		//Routing keys for RPC subscriptions should include the reply-to queueName as the final segment.
		return getCreatedQueueName();
	}

	@Override
	@Test 
	public void unsubscribingShouldNotStopPollingOnOtherQueues() throws InterruptedException{
		//The first (i.e. the "other" ) subscription must not be one that automatically unsubscribes.
		delegateFirstSubscribeCallToBase = true;
	}
	

	private boolean hasStartedListening;
	private boolean hadStartedListeningPriorToEnvelopePublication;
	@Test
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