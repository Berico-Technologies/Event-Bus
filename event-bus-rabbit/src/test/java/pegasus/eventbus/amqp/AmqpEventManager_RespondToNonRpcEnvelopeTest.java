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
//public class AmqpEventManager_RespondToNonRpcEnvelopeTest extends
//		AmqpEventManager_PublishEnvelopeTestBase {
//
//	private UnacceptedMessage unacceptedMessage;
//	private byte[] bytesForMessageOfTypeTestSendEvent = {18,47,0,54,70,9,5,87,0,49,87};
//
//	private void setupIncomingNonRpcMessage() {
//		
//		Envelope envelopeOfTypeTestSendEvent = new Envelope();
//		envelopeOfTypeTestSendEvent.setEventType(TestSendEvent2.class.getCanonicalName());
//		envelopeOfTypeTestSendEvent.setBody(bytesForMessageOfTypeTestSendEvent);
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
//		//Here we set up a scenario where an event handler respondTo a received event
//		//but the event was sent with publish rather than getReponseTo.  This scenario
//		//should simply publish the response according to the routing for that type.
//		setupIncomingNonRpcMessage();
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
//		assertEquals(routingInfo, publishedRoute);
//	}
//
//	@Override
//	@Test 
//	public void theTopicOfThePublishedEnvelopeShouldBeTheRoutingKeyFromTheRoutingInfo(){
//		assertEquals(routingInfo.routingKey, publishedEnvelope.getTopic());
//	}
//
//	@Test 
//	public void thePublishedEnvelopetShouldHaveNoReplyToValueAssigned(){
//		assertNull(publishedEnvelope.getReplyTo());
//	}
//	
//	@Test 
//	public void thePublishedRouteShouldBeTheGenericRouteForTheResponseType(){	
//		assertEquals(routingInfo, publishedRoute);
//	}
//}