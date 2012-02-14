package pegasus.eventbus.rabbitmq;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.BasicProperties;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventResult;

public class EnvelopeHandlerBasedConsumer_MessageAcknowledgementTests {

	
	@Mock private Channel channel;
	@Mock com.rabbitmq.client.Envelope envelope;
	@Mock BasicProperties properties; 

	byte[] body = {95,49,79,28,0,7,6,0,89,55,46};
	
	private long deliveryTag = 389723984;
	
	EnvelopeHandlerBasedConsumer consumer;
	
	@Before
	public void beforeEachTest(){
		 MockitoAnnotations.initMocks(this);
		 when(envelope.getDeliveryTag()).thenReturn(deliveryTag);
	}
	
	@Test
	public void priorToHandlerReturningTheMessageShouldNietherBeAcceptedOrRejctected()
			throws Exception {
		givenAHandlerThatAssertsMessageIsNietherAcceptedNorRejected();
		whenTheMessageIsHandled();
	}

	@Test
	public void whenTheHandlerReturnsHandledTheMessageShouldBeAccepted()
			throws Exception {
		givenAHandlerThatReturns(EventResult.Handled);
		whenTheMessageIsHandled();
		verify(channel).basicAck(deliveryTag, false);
	}

	@Test
	public void whenTheHandlerReturnsFailedTheMessageShouldBeRejectedWithoutRedelivery()
			throws Exception {
		givenAHandlerThatReturns(EventResult.Failed);
		whenTheMessageIsHandled();
		verify(channel).basicReject(deliveryTag, false);
	}

	@Test
	public void whenTheHandlerReturnsRetryTheMessageShouldBeRejectedWithRedelivery()
			throws Exception {
		givenAHandlerThatReturns(EventResult.Retry);
		whenTheMessageIsHandled();
		verify(channel).basicReject(deliveryTag, true);
	}

	@Test
	public void whenTheHandlerThrowsAnExceptionTheMessageShouldBeRejectedWithoutRedelivery()
			throws Exception {
		givenAHandlerThatThrows();
		whenTheMessageIsHandled();
		verify(channel).basicReject(deliveryTag, false);
	}

	protected void givenAHandlerThatAssertsMessageIsNietherAcceptedNorRejected(){
	
		consumer = new EnvelopeHandlerBasedConsumer(channel, "testQueue", new EnvelopeHandler(){

			@Override
			public EventResult handleEnvelope(Envelope envelope) {
				try {
					verify(channel, never()).basicAck(anyLong(), anyBoolean());
					verify(channel, never()).basicReject(anyLong(), anyBoolean());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return EventResult.Handled;
			}

			@Override
			public String getEventSetName() {
				return null;
			}});
	}

	protected void givenAHandlerThatReturns(final EventResult result){
		
		consumer = new EnvelopeHandlerBasedConsumer(channel, "testQueue", new EnvelopeHandler(){

			@Override
			public EventResult handleEnvelope(Envelope envelope) {
								
				return result;
			}

			@Override
			public String getEventSetName() {
				return null;
			}});
	}
	

	protected void givenAHandlerThatThrows(){
		consumer = new EnvelopeHandlerBasedConsumer(channel, "testQueue", new EnvelopeHandler(){

			@Override
			public EventResult handleEnvelope(Envelope envelope) {
								
				throw new RuntimeException("Kaboom!");
			}

			@Override
			public String getEventSetName() {
				return null;
			}});
	}
	
	private void whenTheMessageIsHandled(){
		try {
			consumer.handleDelivery(null, envelope, properties, body);
		} catch (IOException e) {
			fail("handleDelivery threw unexpected exception:" + e.getMessage());
		}
	}
}
	
