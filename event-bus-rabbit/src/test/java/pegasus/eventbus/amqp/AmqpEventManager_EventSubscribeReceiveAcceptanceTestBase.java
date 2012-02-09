//package pegasus.eventbus.amqp;
//
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//import static com.jayway.awaitility.Awaitility.*;
//import static org.hamcrest.Matchers.*;
//
//import java.util.concurrent.TimeUnit;
//
//import org.junit.*;
//
//import pegasus.eventbus.client.EventHandler;
//import pegasus.eventbus.client.EventResult;
//import pegasus.eventbus.testsupport.TestSendEvent;
//import pegasus.eventbus.testsupport.TestSendEvent2;
//
//public abstract class AmqpEventManager_EventSubscribeReceiveAcceptanceTestBase
//	extends AmqpEventManager_SubscribeReceiveAcceptanceTestBase{
//	
//	@Test 
//	public void whenAnEventOfATypeNotHandledByTheHandlerIsReceivedTheMessageShouldBeRejectedWithoutRedelivery() throws Exception {
//		subscribe(new WrongTypeEventHandler());
//		Thread.sleep(100);
//		verify(messageBus).rejectMessage(unacceptedMessage, false);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test 
//	public void whenAnEventThatCannotBeDeserializedIsReceivedTheMessageShouldBeRejectedWithoutRedelivery() throws Exception {
//		reset(serializer);
//		when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent, TestSendEvent.class)).thenThrow(RuntimeException.class);
//		manager.subscribe(new ResultSpecifyingEventHandler(EventResult.Handled));
//		Thread.sleep(100);
//		verify(messageBus).rejectMessage(unacceptedMessage, false);
//	}
//	
//	protected abstract void subscribe(EventHandler<?> eventHandler);
//
//	@Override 
//	protected void subscribeAHandlerThatAssertsMessageIsNietherAcceptedNorRejected(){
//		subscribeAndWaitForMessage(new NonAcceptanceAssertingEventHandler());
//	}
//
//	@Override 
//	protected void subscribeAHandlerThatReturns(EventResult result){
//		subscribeAndWaitForMessage(new ResultSpecifyingEventHandler(result));
//	}
//
//	@Override 
//	protected void subscribeAHandlerThatThrows(){
//		subscribeAndWaitForMessage(new ThrowingEventHandler());
//	}
//	
//	protected void subscribeAndWaitForMessage(ResponseTrackingEventHandler testEventHandler) {
//		subscribe(testEventHandler);
//		
//		try { 
//			waitAtMost(1, TimeUnit.SECONDS).untilCall(to(testEventHandler).isEventHandled(), equalTo(true));
//		} catch (Exception e) {
//			fail("Events not received by handler within expected time period.");
//		} finally{
//			manager.close();
//		}
//	}
//
//	public class ResponseTrackingEventHandler implements EventHandler<TestSendEvent>{
//		
//		private volatile boolean eventHandled;
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public Class<? extends TestSendEvent>[] getHandledEventTypes() {
//			Class<?>[] handledTypes = {TestSendEvent.class};
//			return  (Class<? extends TestSendEvent>[]) handledTypes;
//		}
//
//		@Override
//		public EventResult handleEvent(TestSendEvent event) {
//			eventHandled = true;
//			return EventResult.Handled;
//		}
//
//		public boolean isEventHandled() {
//			return eventHandled;
//		}
//	}
//	
//	public class ResultSpecifyingEventHandler extends ResponseTrackingEventHandler{
//		private EventResult resultToReturn;
//		
//		public ResultSpecifyingEventHandler(EventResult resultToReturn) {
//			super();
//			this.resultToReturn = resultToReturn;
//		}
//
//
//		@Override
//		public EventResult handleEvent(TestSendEvent event) {
//			super.handleEvent(event);
//			return resultToReturn;
//		}
//	}
//	
//	public class NonAcceptanceAssertingEventHandler extends ResponseTrackingEventHandler{
//
//		@Override
//		public EventResult handleEvent(TestSendEvent event) {
//			super.handleEvent(event);
//			verify(messageBus, never()).acceptMessage(unacceptedMessage);
//			verify(messageBus, never()).rejectMessage(unacceptedMessage, true);
//			verify(messageBus, never()).rejectMessage(unacceptedMessage, false);
//			return EventResult.Handled;
//		}
//	}
//	
//	public class ThrowingEventHandler extends ResponseTrackingEventHandler{
//		
//		@Override
//		public EventResult handleEvent(TestSendEvent event) {
//			super.handleEvent(event);
//			throw new RuntimeException();
//		}
//	}
//	
//	public class WrongTypeEventHandler implements EventHandler<Object>{
//		@Override
//		public Class<? extends Object>[] getHandledEventTypes() {
//			Class<?>[] handledTypes = {TestSendEvent2.class};
//			return  (Class<? extends Object>[]) handledTypes;
//		}
//
//		@Override
//		public EventResult handleEvent(Object event) {
//			return EventResult.Handled;
//		}
//	}
//}
