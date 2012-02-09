package pegasus.eventbus.amqp;

import static com.jayway.awaitility.Awaitility.to;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.testsupport.TestSendEvent;

public abstract class AmqpEventManager_SubscribeReceiveAcceptanceTestBase
		extends AmqpEventManager_TestBase {

	protected TestableUnacceptedMessage unacceptedMessage;
	protected byte[] bytesForMessageOfTypeTestSendEvent = { 1 };

	public AmqpEventManager_SubscribeReceiveAcceptanceTestBase() {
		super();
	}

	@Before
	@Override
	public void beforeEachTest() {

		super.beforeEachTest();

		Envelope envelopeOfTypeTestSendEvent = new Envelope();
		envelopeOfTypeTestSendEvent.setEventType(TestSendEvent.class.getCanonicalName());
		envelopeOfTypeTestSendEvent.setBody(bytesForMessageOfTypeTestSendEvent);

		when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent, TestSendEvent.class))
			.thenReturn(new TestSendEvent());

		unacceptedMessage = new TestableUnacceptedMessage(envelopeOfTypeTestSendEvent, 1);

		when(messageBus.getNextMessageFrom(anyString()))
			.thenReturn(unacceptedMessage)
			.thenReturn(null);
	}

	@Test
	public void priorToHandlerReturningTheMessageShouldNietherBeAcceptedOrRejctected()
			throws Exception {
		subscribeAHandlerThatAssertsMessageIsNietherAcceptedNorRejected();
	}

	@Test
	public void whenTheHandlerReturnsHandledTheMessageShouldBeAccepted()
			throws Exception {
		subscribeAHandlerThatReturns(EventResult.Handled);
		verify(messageBus).acceptMessage(unacceptedMessage);
	}

	@Test
	public void whenTheHandlerReturnsFailedTheMessageShouldBeRejectedWithoutRedelivery()
			throws Exception {
		subscribeAHandlerThatReturns(EventResult.Failed);
		verify(messageBus).rejectMessage(unacceptedMessage, false);
	}

	@Test
	public void whenTheHandlerReturnsRetryTheMessageShouldBeRejectedWithRedelivery()
			throws Exception {
		subscribeAHandlerThatReturns(EventResult.Retry);
		verify(messageBus).rejectMessage(unacceptedMessage, true);
	}

	@Test
	public void whenTheHandlerThrowsAnExceptionTheMessageShouldBeRejectedWithoutRedelivery()
			throws Exception {
		subscribeAHandlerThatThrows();
		verify(messageBus).rejectMessage(unacceptedMessage, false);
	}

	@Test
	public void whenTheMessageBusThrowsAnExceptionTheSubscriptionShouldContinueProcessingMessages() 
			throws Exception{
		
		reset(messageBus);
		when(messageBus.getNextMessageFrom(anyString()))
			.thenThrow(new RuntimeException("Something went wrong."))
			.thenReturn(unacceptedMessage)
			.thenReturn(null);
	
		subscribeAHandlerThatReturns(EventResult.Handled);

		waitAtMost(1, TimeUnit.SECONDS).untilCall(to(unacceptedMessage).wasMessageRead(), equalTo(true));
	
		verify(messageBus, atLeast(2)).getNextMessageFrom(anyString());
	}

	protected abstract void subscribeAHandlerThatAssertsMessageIsNietherAcceptedNorRejected();

	protected abstract void subscribeAHandlerThatReturns(EventResult result);

	protected abstract void subscribeAHandlerThatThrows();
	
	private class TestableUnacceptedMessage extends UnacceptedMessage{
		public TestableUnacceptedMessage(Envelope envelope,
				long acknowledgementToken) {
			super(envelope, acknowledgementToken);
			// TODO Auto-generated constructor stub
		}

		private boolean wasRead;
		
		@Override
		public Envelope getEnvelope() {
			wasRead = true;
			return super.getEnvelope();
		}

		public boolean wasMessageRead() {
			return wasRead;
		}
	}
}