//package pegasus.eventbus.amqp;
//
//import static org.junit.Assert.*;
//import static com.jayway.awaitility.Awaitility.*;
//import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.*;
//
//import java.util.concurrent.TimeUnit;
//
//import org.junit.Test;
//
//import pegasus.eventbus.client.Envelope;
//import pegasus.eventbus.client.EventHandler;
//import pegasus.eventbus.client.EventResult;
//import pegasus.eventbus.client.FallbackDetails;
//import pegasus.eventbus.client.FallbackDetails.FallbackReason;
//import pegasus.eventbus.client.FallbackHandler;
//import pegasus.eventbus.testsupport.TestSendEvent;
//
//public class AmqpEventManager_SubscribeReceiveAcceptanceWithFallbackTest extends
//AmqpEventManager_EventSubscribeReceiveAcceptanceTestBase {
//
//	private ResponseTrackingFallbackHandler fallbackHandler = new ResponseTrackingFallbackHandler();
//	private boolean waitForFallbackHandler = false;
//	@Override
//	protected void subscribe(EventHandler<?> eventHandler) {
//		manager.subscribe(eventHandler, fallbackHandler);
//	}
//	
//	@Override
//	protected void subscribeAndWaitForMessage(ResponseTrackingEventHandler testEventHandler) {
//		if(!waitForFallbackHandler){
//			super.subscribeAndWaitForMessage(testEventHandler);
//			return;
//		}
//			
//		subscribe(testEventHandler);
//		
//		try { 
//			waitAtMost(100, TimeUnit.SECONDS).untilCall(to(fallbackHandler).handlerWasCalled(), equalTo(true));
//		} catch (Exception e) {
//			fail("Events not received by handler within expected time period.");
//		} finally{
//			manager.close();
//		}
//	}
//
//	private class ResponseTrackingFallbackHandler implements FallbackHandler{
//
//		private FallbackDetails details;
//
//		@Override
//		public EventResult handleEnvelope(Envelope envelope, FallbackDetails details) {
//			this.details = details;
//			return EventResult.Failed;
//		}
//		
//		public boolean handlerWasCalled(){
//			return details != null;
//		}
//
//		public FallbackReason getFallbackReason(){
//			return details.getReason();
//		}
//		
//		public Exception getFallbackException(){
//			return details.getException();
//		}
//	}
//	
//	public class NonAcceptanceAssertingFallbackHandler extends ResponseTrackingFallbackHandler {
//
//		@Override
//		public EventResult handleEnvelope(Envelope envelope, FallbackDetails details) {
//			verify(messageBus, never()).acceptMessage(unacceptedMessage);
//			verify(messageBus, never()).rejectMessage(unacceptedMessage, true);
//			verify(messageBus, never()).rejectMessage(unacceptedMessage, false);
//			return super.handleEnvelope(envelope, details);
//		}
//	}
//	
//	public class FallbackHandlerThatThrows extends ResponseTrackingFallbackHandler {
//
//		@Override
//		public EventResult handleEnvelope(Envelope envelope, FallbackDetails details) {
//			super.handleEnvelope(envelope, details);
//			throw new RuntimeException();
//		}
//	}
//	
//	public class FallbackHandlerThatReturns extends ResponseTrackingFallbackHandler {
//
//		private final EventResult result;
//		
//		public FallbackHandlerThatReturns(EventResult result) {
//			super();
//			this.result = result;
//		}
//
//		@Override
//		public EventResult handleEnvelope(Envelope envelope, FallbackDetails details) {
//			super.handleEnvelope(envelope, details);
//			return result;
//		}
//	}
//	
//	@Test 
//	public void whenTheEventHandlerReturnsHandledTheFallbackHandlerShouldNotBeInvoked() 
//			throws InterruptedException{
//		subscribeAHandlerThatReturns(EventResult.Handled);
//		Thread.sleep(100);
//		assertFalse(fallbackHandler.handlerWasCalled());
//	}
//	
//	@Test 
//	public void whenTheEventHandlerReturnsRetryTheFallbackHandlerShouldNotBeInvoked() 
//			throws InterruptedException{
//		subscribeAHandlerThatReturns(EventResult.Retry);
//		Thread.sleep(100);
//		assertFalse(fallbackHandler.handlerWasCalled());
//	}
//	
//	@Test 
//	public void whenTheEventHandlerReturnsFailedTheFallbackHandlerShouldBeInvokedWithReasonOfEventHandlerReturnedFailure() 
//			throws InterruptedException{
//
//		waitForFallbackHandler = true;		
//		subscribeAHandlerThatReturns(EventResult.Failed);
//		
//		assertTrue(fallbackHandler.handlerWasCalled());
//		assertEquals(FallbackReason.EventHandlerReturnedFailure, fallbackHandler.getFallbackReason());
//		assertNull(fallbackHandler.getFallbackException());
//	}
//	
//	@Test 
//	public void whenTheEventHandlerThrowsTheFallbackHandlerShouldBeInvokedWithReasonOfEventHandlerThrewException() 
//			throws InterruptedException{
//
//		waitForFallbackHandler = true;
//		subscribeAHandlerThatThrows();
//		
//		assertTrue(fallbackHandler.handlerWasCalled());
//		assertEquals(FallbackReason.EventHandlerThrewException, fallbackHandler.getFallbackReason());
//		assertNotNull(fallbackHandler.getFallbackException());
//	}
//
//	@Test 
//	public void whenTheSerializerThrowsTheFallbackHandlerShouldBeInvokedWithReasonOfDeserializationError() 
//			throws InterruptedException{
//
//		waitForFallbackHandler = true;
//		RuntimeException expectedException = new RuntimeException();
//		
//		reset(serializer);
//		when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent, TestSendEvent.class))
//			.thenThrow(expectedException);
//		
//		subscribeAHandlerThatReturns(EventResult.Handled);
//		
//		assertTrue(fallbackHandler.handlerWasCalled());
//		assertEquals(FallbackReason.DeserializationError, fallbackHandler.getFallbackReason());
//		assertSame(expectedException, fallbackHandler.getFallbackException());
//	}
//
//	@Test
//	public void priorToTheFallbackHandlerReturningTheMessageShouldNietherBeAcceptedOrRejctected()
//			throws Exception {
//		waitForFallbackHandler = true;
//		fallbackHandler = new NonAcceptanceAssertingFallbackHandler();
//		subscribeAHandlerThatReturns(EventResult.Failed);
//	}
//
//	@Test
//	public void whenTheFallbackHandlerReturnsHandledTheMessageShouldBeAccepted()
//			throws Exception {
//		waitForFallbackHandler = true;
//		fallbackHandler = new FallbackHandlerThatReturns(EventResult.Handled);
//		subscribeAHandlerThatReturns(EventResult.Failed);
//		verify(messageBus).acceptMessage(unacceptedMessage);
//	}
//
//	@Test
//	public void whenTheFallbackHandlerReturnsFailedTheMessageShouldBeRejectedWithoutRedelivery()
//			throws Exception {
//		waitForFallbackHandler = true;
//		fallbackHandler = new FallbackHandlerThatReturns(EventResult.Failed);
//		subscribeAHandlerThatReturns(EventResult.Failed);
//		verify(messageBus).rejectMessage(unacceptedMessage, false);
//	}
//
//	@Test
//	public void whenTheFallbackHandlerReturnsRetryTheMessageShouldBeRejectedWithRedelivery()
//			throws Exception {
//		waitForFallbackHandler = true;
//		fallbackHandler = new FallbackHandlerThatReturns(EventResult.Retry);
//		subscribeAHandlerThatReturns(EventResult.Failed);
//		verify(messageBus).rejectMessage(unacceptedMessage, true);
//	}
//
//	@Test
//	public void whenTheFallbackHandlerThrowsAnExceptionTheMessageShouldBeRejectedWithoutRedelivery()
//			throws Exception {
//		waitForFallbackHandler = true;
//		fallbackHandler = new FallbackHandlerThatThrows();
//		subscribeAHandlerThatReturns(EventResult.Failed);
//		verify(messageBus).rejectMessage(unacceptedMessage, false);
//	}
//}