package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.*;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.testsupport.TestSendEvent;

/**
 * Test fixture so assert that calling AmqpEventManger.Publish correctly invokes AmqpMessaegBus.Pushlish
 */
public abstract class AmqpEventManager_PublishEnvelopeTestBase extends AmqpEventManager_TestBase{

	protected byte[] bytesFromSerializer = {39,84,72,30,87,50,98,75,0};
	
	protected RoutingInfo publishedRoute;

	protected Envelope publishedEnvelope;

	@Before
	@Override
	public void beforeEachTest() {
		
		super.beforeEachTest();
		
		when(eventTopicMapper.getTopicFor(TestSendEvent.class)).thenReturn("test-topic");
		
		when(routingProvider.getRoutingInfoFor("test-topic")).thenReturn(routingInfo);
		
		when(serializer.serialize(sendEvent)).thenReturn(bytesFromSerializer);

		publish();
		
		ArgumentCaptor<RoutingInfo> routingCaptor = ArgumentCaptor.forClass(RoutingInfo.class);
		ArgumentCaptor<Envelope> envelopeCaptor = ArgumentCaptor.forClass(Envelope.class);
		
		verify(messageBus).publish(routingCaptor.capture(), envelopeCaptor.capture());
		
		publishedRoute = routingCaptor.getValue();
		publishedEnvelope = envelopeCaptor.getValue();
	}
	
	protected abstract void publish();
	
	@Test 
	public void theEnvelopeShouldBePlacedOnTheBusUsingTheRoutingInformationFromTheRoutingProvider(){
		assertEquals(routingInfo, publishedRoute);
	}
	
	@Test 
	public void theBodyOfPublishedEnvelopeShouldBeThatOfTheSerialziedEvent(){
		assertArrayEquals(bytesFromSerializer, publishedEnvelope.getBody());
	}
	
	@Test 
	public void thePublishedEnvelopeShouldHaveAnIdAssigned(){
		assertNotNull(publishedEnvelope.getId());
	}
	
	@Test 
	public void thePublishedEnvelopeShouldNotHaveACorrelationIdAssigned(){
		assertNull(publishedEnvelope.getCorrelationId());
	}
	
	@Test 
	public void theEventTypeOfThePublishedEnvelopeShouldBeTheCannonicalNameOfTheEventsClass(){
		assertEquals(sendEvent.getClass().getCanonicalName(), publishedEnvelope.getEventType());
	}
	
	@Test 
	public void theTopicOfThePublishedEnvelopeShouldBeTheRoutingKeyFromTheRoutingInfo(){
		assertEquals(routingInfo.routingKey, publishedEnvelope.getTopic());
	}
}
