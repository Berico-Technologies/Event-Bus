//package pegasus.eventbus.amqp;
//
//import static com.jayway.awaitility.Awaitility.*;
//import static org.hamcrest.Matchers.*;
//import static org.junit.Assert.*;
//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.*;
//
//import java.util.concurrent.TimeUnit;
//
//import org.junit.*;
//
//import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
//import pegasus.eventbus.client.Envelope;
//import pegasus.eventbus.client.EventHandler;
//import pegasus.eventbus.client.EventResult;
//import pegasus.eventbus.client.SubscriptionToken;
//import pegasus.eventbus.testsupport.TestSendEvent2;
//
//public class AmqpEventManager_RespondToRpcEnvelopeTest extends
//		AmqpEventManager_PublishEnvelopeTestBase {
//
//	private static final String REPLY_TO_QUEUE = "test_reply_to_queue";
//	
//	private UnacceptedMessage unacceptedMessage;
//	private byte[] bytesForMessageOfTypeTestSendEvent = {18,47,0,54,70,9,5,87,0,49,87};
//
//	private RoutingInfo expectedRoute;
//
//	@Override
//	public void beforeEachTest(){
//		super.beforeEachTest();
//		expectedRoute = new RoutingInfo(
//				routingInfo.getExchange(), 
//				routingInfo.routingKey + AmqpEventManager.AMQP_ROUTE_SEGMENT_DELIMITER + REPLY_TO_QUEUE);
//	}
//	
//	private void setupIncomingMessage() {
//		
//		Envelope envelopeOfTypeTestSendEvent = new Envelope();
//		envelopeOfTypeTestSendEvent.setEventType(TestSendEvent2.class.getCanonicalName());
//		envelopeOfTypeTestSendEvent.setBody(bytesForMessageOfTypeTestSendEvent);
//		envelopeOfTypeTestSendEvent.setReplyTo(REPLY_TO_QUEUE);
//		
//		when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent, TestSendEvent2.class)).thenReturn(new TestSendEvent2());
//		
//		unacceptedMessage = new UnacceptedMessage(envelopeOfTypeTestSendEvent,1);
//		
//		when(messageBus.getNextMessageFrom(anyString()))
//			.thenReturn(unacceptedMessage)
//			.thenReturn(null);		
//	}
//	
//	@Override
//	protected void publish() {
//		//Replies can only be sent in response to an actually received event so 
//		//our reply-publish action is done by the RpcEventHandler in response to
//		//a mock incoming event.
//		setupIncomingMessage();
//		
//		RpcEventHandler handler = new RpcEventHandler();
//		
//		SubscriptionToken token = manager.subscribe(handler);
//		try {
//			waitAtMost(1, TimeUnit.SECONDS).untilCall(to(handler).getEventHandled(), equalTo(true));
//		} catch (Exception e) {
//			fail(e.getMessage());
//			e.printStackTrace();
//		} finally {
//			manager.unsubscribe(token);
//		}
//	}
//
//	private class RpcEventHandler implements EventHandler<TestSendEvent2>{
//
//		private volatile boolean eventHandled;
//		
//		@SuppressWarnings("unchecked")
//		@Override
//		public Class<? extends TestSendEvent2>[] getHandledEventTypes() {
//			Class<?>[] handledTypes = {TestSendEvent2.class};
//			return  (Class<? extends TestSendEvent2>[]) handledTypes;
//		}
//
//		@Override
//		public EventResult handleEvent(TestSendEvent2 event) {
//			manager.respondTo(event, sendEvent);
//			eventHandled = true;
//			return EventResult.Handled;
//		}
//		
//		public boolean getEventHandled(){
//			return eventHandled;
//		}
//	}
//	
//	@Override
//	@Test 
//	public void theEnvelopeShouldBePlacedOnTheBusUsingTheRoutingInformationFromTheRoutingProvider(){
//		assertEquals(expectedRoute, publishedRoute);
//	}
//
//	@Override
//	@Test 
//	public void theTopicOfThePublishedEnvelopeShouldBeTheRoutingKeyFromTheRoutingInfo(){
//		assertEquals(expectedRoute.routingKey, publishedEnvelope.getTopic());
//	}
//
//	@Test 
//	public void thePublishedEnvelopetShouldHaveTheSameReplyToValueAssignedAsTheReceivedEnvelope(){
//		assertEquals(REPLY_TO_QUEUE, publishedEnvelope.getReplyTo());
//	}
//	
//	@Test 
//	public void thePublishedRouteShouldBeTheRouteForTheResponseTypePlusTheReplyToValue(){	
//		assertEquals(expectedRoute, publishedRoute);
//	}
//}