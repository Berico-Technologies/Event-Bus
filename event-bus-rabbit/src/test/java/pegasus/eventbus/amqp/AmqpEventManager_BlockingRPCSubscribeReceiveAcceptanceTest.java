//package pegasus.eventbus.amqp;
//
//import static org.junit.Assert.*;
//import static org.hamcrest.Matchers.*;
//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.*;
//
//import java.util.concurrent.TimeoutException;
//
//import org.apache.commons.lang.time.StopWatch;
//import org.junit.*;
//
//import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
//import pegasus.eventbus.client.Envelope;
//import pegasus.eventbus.testsupport.TestResponseEvent;
//
//public class AmqpEventManager_BlockingRPCSubscribeReceiveAcceptanceTest extends AmqpEventManager_TestBase{
//	
//	private UnacceptedMessage unacceptedMessage;
//	private byte[] bytesOfResponseMessage = {1};
//	private TestResponseEvent deserializedResponse = new TestResponseEvent();
//
//	@Before
//	@Override
//	public void beforeEachTest() {
//		
//		super.beforeEachTest();
//
//		Envelope responseEnvelope = new Envelope();
//		responseEnvelope.setEventType(TestResponseEvent.class.getCanonicalName());
//		responseEnvelope.setBody(bytesOfResponseMessage);
//		
//		when(serializer.deserialize(bytesOfResponseMessage, TestResponseEvent.class)).thenReturn(deserializedResponse);
//		
//		unacceptedMessage = new UnacceptedMessage(responseEnvelope,1);
//		
//		when(messageBus.getNextMessageFrom(anyString()))
//			.thenReturn(unacceptedMessage)
//			.thenReturn(null);		
//	}
//	
//	@Test 
//	public void getResponseToShouldReturnTheDeserializedResponse() 
//			throws InterruptedException, TimeoutException  {
//		@SuppressWarnings({ "unchecked" })
//		TestResponseEvent response = manager.getResponseTo(sendEvent, 100, TestResponseEvent.class);
//		assertEquals(deserializedResponse, response);
//	}
//
//	@Test 
//	public void getResponseToShouldReturnTheDeserializedResponseAsSoonAsReceivedNotAtEndOfTimeout() 
//			throws InterruptedException, TimeoutException  {
//		
//		StopWatch watch = new StopWatch();
//		watch.start();
//		@SuppressWarnings({ "unchecked", "unused" })
//		TestResponseEvent response = manager.getResponseTo(sendEvent, 1000, TestResponseEvent.class);
//		watch.stop();
//		assertThat(watch.getTime(), lessThan(200l));
//	}
//	
//	@Test(expected=TimeoutException.class) 
//	public void getResponseToShouldThrowIfAResponseIsNotReceivedWithinTheTimeoutPeriod() 
//			throws InterruptedException, TimeoutException  {
//		messageBus.getNextMessageFrom("?"); //Hackish... This discards the waiting message so no message will be received thus ensuring a timeout.
//		
//		@SuppressWarnings({ "unchecked", "unused" })
//		TestResponseEvent response = manager.getResponseTo(sendEvent, 5, TestResponseEvent.class);
//		
//	}
//	
//	@Test 
//	public void whenGetResponseToReturnsThenTheResponseMessageShouldBeAccepted() 
//			throws InterruptedException, TimeoutException {
//		@SuppressWarnings({ "unchecked", "unused" })
//		TestResponseEvent response = manager.getResponseTo(sendEvent, 100, TestResponseEvent.class);
//		verify(messageBus).acceptMessage(unacceptedMessage);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test 
//	public void whenAnEventThatCannotBeDeserializedIsReceivedTheMessageShouldBeRejectedWithoutRedelivery() 
//			throws InterruptedException {
//		reset(serializer);
//		when(serializer.deserialize(bytesOfResponseMessage, TestResponseEvent.class)).thenThrow(RuntimeException.class);
//		try {
//			@SuppressWarnings({ "unused" })
//			TestResponseEvent response = manager.getResponseTo(sendEvent, 10, TestResponseEvent.class);
//		} catch (TimeoutException e) {
//			//we expect this to happen.
//		}
//		verify(messageBus).rejectMessage(unacceptedMessage, false);
//	}
//
//}
