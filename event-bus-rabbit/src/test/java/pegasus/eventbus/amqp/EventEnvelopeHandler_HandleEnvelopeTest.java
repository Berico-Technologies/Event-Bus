package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.testsupport.TestResponseEvent;
import pegasus.eventbus.testsupport.TestSendEvent;

public class EventEnvelopeHandler_HandleEnvelopeTest{
	
	@Mock private AmqpEventManager eventManager;
	@Mock private Serializer serializer;
	
	Map<Object, Envelope> eventEnvelopeMap = new HashMap<Object, Envelope>();
	
	private EventEnvelopeHandler envelopeHandler;
	private TestableEventHandler eventHandler;
	private Envelope envelope = new Envelope();
	private byte[] goodBody = {49,83,15,98,72,43,89,57};
	private byte[] badBody = {0};
	
	@Before
	public void beforeEachTest(){
		
		MockitoAnnotations.initMocks(this);
		
		when(eventManager.getSerializer()).thenReturn(serializer);
		when(eventManager.getEnvelopesBeingHandled()).thenReturn(eventEnvelopeMap);
		when(serializer.deserialize(goodBody, TestSendEvent.class)).thenReturn(new TestSendEvent());
		when(serializer.deserialize(badBody, TestSendEvent.class)).thenThrow(new RuntimeException());
		
		envelope = new Envelope();
		envelope.setEventType(TestSendEvent.class.getCanonicalName());
		envelope.setBody(goodBody);
}
	
	@Test 
	public void whenAnEventOfATypeNotHandledByTheEventHandlerIsReceivedTheEnvelopeHandlerShouldReturnFailed() {
		
		givenAHandlerThatHandlesTestSendEvents();
		
		envelope.setEventType(TestResponseEvent.class.getCanonicalName());
		
		EventResult result = envelopeHandler.handleEnvelope(envelope);

		assertFalse(eventHandler.getHandlerWasInvoked());
		assertEquals(EventResult.Failed, result);
		assertThatTheListOfInProcessEventNoLongerContainsTheHandledEvent();
	}

	@Test 
	public void whenAnEventThatCannotBeDeserializedIsReceivedTheEnvelopeHandlerShouldReturnFailed() throws Exception {
		
		givenAHandlerThatHandlesTestSendEvents();

		envelope.setBody(badBody);
		
		EventResult result = envelopeHandler.handleEnvelope(envelope);

		assertFalse(eventHandler.getHandlerWasInvoked());
		assertEquals(EventResult.Failed, result);
		assertThatTheListOfInProcessEventNoLongerContainsTheHandledEvent();
	}
	
	@Test 
	public void whenTheEventHandlerThrowsAnExceptionTheEnvelopeHandlerShouldReturnFailed() throws Exception {
		
		givenAHandlerThatThrows();
		
		EventResult result = envelopeHandler.handleEnvelope(envelope);

		assertTrue(eventHandler.getHandlerWasInvoked());
		assertEquals(EventResult.Failed, result);
		assertThatTheListOfInProcessEventContainsTheHandledEventWhileItWasBeingHandled();
		assertThatTheListOfInProcessEventNoLongerContainsTheHandledEvent();
	}

	@Test 
	public void whenTheEventHandlerReturnsHandledTheEnvelopeHandlerShouldReturnHandled()  {
		
		givenAHandlerThatReturns(EventResult.Handled);
		
		EventResult result = envelopeHandler.handleEnvelope(envelope);

		assertTrue(eventHandler.getHandlerWasInvoked());
		assertEquals(EventResult.Handled, result);
		assertThatTheListOfInProcessEventContainsTheHandledEventWhileItWasBeingHandled();
		assertThatTheListOfInProcessEventNoLongerContainsTheHandledEvent();
	}

	@Test 
	public void whenTheEventHandlerReturnsFailedTheEnvelopeHandlerShouldReturnFailed()  {
		
		givenAHandlerThatReturns(EventResult.Failed);
		
		EventResult result = envelopeHandler.handleEnvelope(envelope);

		assertEquals(EventResult.Failed, result);
		assertTrue(eventHandler.getHandlerWasInvoked());
		assertThatTheListOfInProcessEventContainsTheHandledEventWhileItWasBeingHandled();
		assertThatTheListOfInProcessEventNoLongerContainsTheHandledEvent();
	}

	@Test 
	public void whenTheEventHandlerReturnsRetryTheEnvelopeHandlerShouldReturnRetry()  {
		
		givenAHandlerThatReturns(EventResult.Retry);
		
		EventResult result = envelopeHandler.handleEnvelope(envelope);

		assertEquals(EventResult.Retry, result);
		assertTrue(eventHandler.getHandlerWasInvoked());
		assertThatTheListOfInProcessEventContainsTheHandledEventWhileItWasBeingHandled();
		assertThatTheListOfInProcessEventNoLongerContainsTheHandledEvent();
	}

	private void givenAHandlerThatHandlesTestSendEvents(){
		eventHandler = new ResultSpecifyingEventHandler(EventResult.Handled);
		envelopeHandler = new EventEnvelopeHandler(eventManager, eventHandler); 
	}
	
	private void givenAHandlerThatThrows(){
		eventHandler = new ThrowingEventHandler();
		envelopeHandler = new EventEnvelopeHandler(eventManager, eventHandler); 
	}
	
	private void givenAHandlerThatReturns(EventResult result) {
		eventHandler = new ResultSpecifyingEventHandler(result);
		envelopeHandler = new EventEnvelopeHandler(eventManager, eventHandler); 
	}

	private void assertThatTheListOfInProcessEventContainsTheHandledEventWhileItWasBeingHandled(){
		assertEquals(envelope, eventHandler.getEnvelopeBeingHandledAtTimeOfInvocation());
	}

	private void assertThatTheListOfInProcessEventNoLongerContainsTheHandledEvent(){
		assertFalse(eventEnvelopeMap.containsValue(envelope));
	}
	
	public abstract class TestableEventHandler implements EventHandler<TestSendEvent>{
		
		boolean handlerWasInvoked;
		Envelope mappedEnvelope;
				
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends TestSendEvent>[] getHandledEventTypes() {
			Class<?>[] handledTypes = {TestSendEvent.class};
			return  (Class<? extends TestSendEvent>[]) handledTypes;
		}

		@Override
		public EventResult handleEvent(TestSendEvent event) {
			handlerWasInvoked = true;
			mappedEnvelope = eventEnvelopeMap.get(event);
			return EventResult.Handled;
		}

		public boolean getHandlerWasInvoked() {
			return handlerWasInvoked;
		}

		public Envelope getEnvelopeBeingHandledAtTimeOfInvocation() {
			return mappedEnvelope;
		}
	}
	
	public class ResultSpecifyingEventHandler extends TestableEventHandler{
		private EventResult resultToReturn;

		public ResultSpecifyingEventHandler(EventResult resultToReturn) {
			super();
			this.resultToReturn = resultToReturn;
		}


		@Override
		public EventResult handleEvent(TestSendEvent event) {
			super.handleEvent(event);
			return resultToReturn;
		}
	}
	
	public class ThrowingEventHandler extends TestableEventHandler{
		
		@Override
		public EventResult handleEvent(TestSendEvent event) {
			super.handleEvent(event);
			throw new RuntimeException();
		}
	}
}
