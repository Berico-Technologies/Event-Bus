package pegasus.eventbus.amqp;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pegasus.eventbus.amqp.AmqpMessageBus.UnexpectedConnectionCloseListener;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.SubscriptionToken;
import pegasus.eventbus.testsupport.TestSendEvent;
import pegasus.eventbus.testsupport.TestSendEvent2;

public class AmqpEventManager_ConnectionResetTests extends
		AmqpEventManager_TestBase {
	
	private UnexpectedConnectionCloseListener conectionCloseListener;

	@Override
	public void beforeEachTest() {
		super.beforeEachTest();

		doAnswer(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				conectionCloseListener = (UnexpectedConnectionCloseListener) invocation.getArguments()[0];
				return null;
			}}).when(messageBus).attachUnexpectedConnectionCloseListener(any(UnexpectedConnectionCloseListener.class));
	
		manager.start();
	}
	
	@Test
	public void whenAConnectionIsClosedAndReopenedAllActiveSubscriptionsShouldBeResubscribed(){
		
		final String expectedQueue;
		EnvelopeHandler expectedHandler;

		ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<EnvelopeHandler> handlerCaptor = ArgumentCaptor.forClass(EnvelopeHandler.class);
        
		manager.subscribe(new TestEventHandler(TestSendEvent.class));

		verify(messageBus, times(1)).beginConsumingMessages(queueNameCaptor.capture(), handlerCaptor.capture());
		expectedQueue = queueNameCaptor.getValue();
		expectedHandler = handlerCaptor.getValue();
		
		SubscriptionToken token2 = manager.subscribe(new TestEventHandler(TestSendEvent2.class));
		
		reset(messageBus);
		
		manager.unsubscribe(token2);
		conectionCloseListener.onUnexpectedConnectionClose(true);
		
		//Assert that the active subscription is re-subscribed
		verify(messageBus, times(1)).beginConsumingMessages(expectedQueue, expectedHandler);
		
		//Asserts that only the active subscription is re-subscribed
		verify(messageBus, times(1)).beginConsumingMessages(anyString(), any(EnvelopeHandler.class));
	}
}
