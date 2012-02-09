package pegasus.eventbus.rabbitmq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.client.Envelope;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;

@RunWith(value = Parameterized.class)
public class RabbitMessageBus_PusblishTest extends RabbitMessageBus_TestBase {

	@Parameters
	public static Collection<Object[]> testEnvelopesToPublish(){
		
		Object[][] data = new Object[][] { 
				{ getNormalEnvelope(), "Normal envelope." },
				//One can argue that an empty envelope is an invalid state, however than any particular property is null is not invalid.
				//Moreover at this level of the code, we should just publish what is given, robustly.  
				{ getEmptyEnvelope(), "Empty envelope." }
				};
		return Arrays.asList(data);
	}

	private static Envelope getNormalEnvelope() {
		Envelope envelope = new Envelope();
		envelope.setBody(new byte[] {35,74,3,50,93,19,3,83,29,2});
		envelope.getHeaders().put(CUSTOM_HEADER_NAME, "CustomHeaderValue");	
		envelope.setId(UUID.randomUUID());
		envelope.setCorrelationId(UUID.randomUUID());
		envelope.setEventType("test.event");
		envelope.setTopic("test.topic");
		envelope.setReplyTo("replyTo.routing_key");
		return envelope;
	}
	
	private static Envelope getEmptyEnvelope() {
		return new Envelope();
	}

	private static final String CUSTOM_HEADER_NAME = "CustomHeader";
	private static final Pattern acceptableUUIDFormatRegEx =  Pattern.compile("^([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}$");

	String testDescription;
	Envelope sentEnvelope;
	GetResponse receivedMessage;
	
	public RabbitMessageBus_PusblishTest(Envelope sentEnvelope, String testDescription) {
		super();
		System.out.println("Test instance: " + testDescription);
		this.sentEnvelope = sentEnvelope;
		this.testDescription = testDescription;
	}

	@Before
	@Override
	public void beforeEachTest() throws IOException{
		
		super.beforeEachTest();

		Connection con = connectionFactory.newConnection();
		Channel channel = con.createChannel();
		String queueName = channel.queueDeclare().getQueue();
		
		rabbitBus.publish(new RoutingInfo("",RoutingInfo.ExchangeType.Direct, true, queueName), sentEnvelope);
		
		receivedMessage = getMessageFromDestination(channel, queueName);
	}
	
	private GetResponse getMessageFromDestination(Channel channel, String queueName)
			throws IOException {
		GetResponse receivedMessage;
		StopWatch timer = new StopWatch();
		timer.start();
		do{
			receivedMessage = channel.basicGet(queueName, true);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				fail("Thread was interupted");
			}
		}while(receivedMessage == null && timer.getTime() < 2000);
		
		assertNotNull("Message not received in time allowed.", receivedMessage);
		
 		return receivedMessage;
	}
	
	@Test
	public void publishShouldPublishMessageToCorrectExchangeAndRoutingKey() throws IOException	{
		assertArrayEquals(sentEnvelope.getBody(),  receivedMessage.getBody());
	}
	
	@Test
	public void publishShouldTransmitEnvelopeIdAsAmqpMessageIdHeader() throws IOException	{
		if(sentEnvelope.getId() == null && receivedMessage.getProps().getMessageId() == null) return;
		assertEquals(sentEnvelope.getId().toString(), receivedMessage.getProps().getMessageId());
	}
	
	@Test
	public void publishShouldTransmitEnvelopeIdInAPlatformIndependentFormat() throws IOException	{
		if(receivedMessage.getProps().getMessageId() == null) return;
		
		assertTrue(acceptableUUIDFormatRegEx.matcher(receivedMessage.getProps().getMessageId()).find());
 	}
	
	@Test
	public void publishShouldTransmitEnvelopeCorrelationIdAsAmqpCorrelationIdHeader() throws IOException	{
		if(sentEnvelope.getCorrelationId() == null && receivedMessage.getProps().getCorrelationId() == null) return;
		assertEquals(sentEnvelope.getCorrelationId().toString(), receivedMessage.getProps().getCorrelationId());
	}
	
	@Test
	public void publishShouldTransmitEnvelopeCorrelationIdInAPlatformIndependentFormat() throws IOException	{
		if(receivedMessage.getProps().getCorrelationId() == null) return;
		
		assertTrue(acceptableUUIDFormatRegEx.matcher(receivedMessage.getProps().getCorrelationId()).find());
 	}
	
	@Test
	public void publishShouldTransmitEventTypeAsAmqpTypeHeader() throws IOException	{
		assertEquals(sentEnvelope.getEventType(), receivedMessage.getProps().getType());
	}
	
	@Test
	public void publishShouldTransmitReplyToAsAmqpReplyToHeader() throws IOException	{
		assertEquals(sentEnvelope.getReplyTo(), receivedMessage.getProps().getReplyTo());
	}
	
	@Test
	public void publishShouldTransmitEnvelopeTopicAsCustomHeader() throws IOException	{
		final Map<String, Object> headers = receivedMessage.getProps().getHeaders();
		if(sentEnvelope.getTopic() == null && !headers.containsKey(RabbitMessageBus.TOPIC_HEADER_KEY)) return;
		assertEquals(sentEnvelope.getTopic(), headers.get(RabbitMessageBus.TOPIC_HEADER_KEY).toString());
	}
	
	@Test
	public void publishShouldTransmitAnyCustomHeaders() throws IOException	{
		final Map<String, Object> headers = receivedMessage.getProps().getHeaders();
		if(!sentEnvelope.getHeaders().containsKey(CUSTOM_HEADER_NAME)  && !headers.containsKey(CUSTOM_HEADER_NAME)) return;
		assertEquals(sentEnvelope.getHeaders().get(CUSTOM_HEADER_NAME), headers.get(CUSTOM_HEADER_NAME).toString());
	}
}