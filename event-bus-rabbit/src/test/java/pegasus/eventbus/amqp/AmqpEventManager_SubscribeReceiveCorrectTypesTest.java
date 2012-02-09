package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.*;

import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.testsupport.TestResponseEvent;
import pegasus.eventbus.testsupport.TestSendEvent;
import pegasus.eventbus.testsupport.TestSendEvent2;

public class AmqpEventManager_SubscribeReceiveCorrectTypesTest extends AmqpEventManager_TestBase{

	private TestSendEvent deserializedTestSendEvent = new TestSendEvent();
	private TestSendEvent2 deserializedTestSendEvent2 = new TestSendEvent2();
	private TestResponseEvent deserializedTestReponseEvent = new TestResponseEvent();
	
	private TestEventHandler eventHandler;

	@Before
	@Override
	public void beforeEachTest() {
		
		super.beforeEachTest();

		Envelope messageOfTypeTestSendEvent = new Envelope();
		byte[] bytesForMessageOfTypeTestSendEvent = {1};
		messageOfTypeTestSendEvent.setEventType(TestSendEvent.class.getCanonicalName());
		messageOfTypeTestSendEvent.setBody(bytesForMessageOfTypeTestSendEvent);
		
		Envelope messageOfTypeTestSendEvent2 = new Envelope();
		byte[] bytesForMessageOfTypeTestSendEvent2 = {2};
		messageOfTypeTestSendEvent2.setEventType(TestSendEvent2.class.getCanonicalName());
		messageOfTypeTestSendEvent2.setBody(bytesForMessageOfTypeTestSendEvent2);
		
		Envelope messageOfTypeTestResponseEvent = new Envelope();
		byte[] bytesForMessageOfTypeTestResponseEvent = {3};
		messageOfTypeTestResponseEvent.setEventType(TestResponseEvent.class.getCanonicalName());
		messageOfTypeTestResponseEvent.setBody(bytesForMessageOfTypeTestResponseEvent);
		
		//when(messageBus.getNextMessageFrom(anyString())).thenReturn(new UnacknowledgedMessage(new Envelope(),1));
		when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent, TestSendEvent.class)).thenReturn(deserializedTestSendEvent);
		when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent2, TestSendEvent2.class)).thenReturn(deserializedTestSendEvent2);
		when(serializer.deserialize(bytesForMessageOfTypeTestResponseEvent, TestResponseEvent.class)).thenReturn(deserializedTestReponseEvent);
		
		when(messageBus.getNextMessageFrom(anyString()))
			.thenReturn(new UnacceptedMessage(messageOfTypeTestResponseEvent,1))
			.thenReturn(new UnacceptedMessage(messageOfTypeTestSendEvent,2))
			.thenReturn(new UnacceptedMessage(messageOfTypeTestSendEvent2,3))
			.thenReturn(null);
		
		eventHandler = new TestEventHandler(TestSendEvent.class, TestSendEvent2.class);
		
		manager.subscribe(eventHandler);
		
		try {
			waitAtMost(1, TimeUnit.SECONDS).untilCall(to(eventHandler.getReceivedEvents()).size(), greaterThanOrEqualTo(2));
		} catch (TimeoutException e) {
			fail("Events not received by handler within expected time period.");
		} catch (Exception e) {
			fail(e.getMessage());
		} finally{
			manager.close();
		}
	}
	
	@Test 
	public void anEventHandlerSubscribedToMultipleEventTypesShouldReceiveEachSubscribedType() throws Exception {
		assertTrue(eventHandler.getReceivedEvents().contains(deserializedTestSendEvent));
		assertTrue(eventHandler.getReceivedEvents().contains(deserializedTestSendEvent2));
	}
	
	@Test 
	public void anEventHandlerShouldNotReceiveTypesNotSubscribedTo() throws Exception {
		assertFalse(eventHandler.getReceivedEvents().contains(deserializedTestReponseEvent));
	}	
}
