package pegasus.eventbus.rabbitmq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
import pegasus.eventbus.client.Envelope;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP.BasicProperties;

@RunWith(value = Parameterized.class)
public class RabbitMessageBus_GetMessageTest extends RabbitMessageBus_TestBase {

	@Parameters
	public static Collection<Object[]> testDataForReception(){
		
		byte[] bytesToSend = {35,74,3,50,93,19,3,83,29,2};
			
		Object[][] data = new Object[][] { 
				{ getNormalProps(), bytesToSend, "Normal envelope and bytes." },
				//One can argue that an empty envelope is an invalid state, however than any particular property is null is not invalid.
				//Moreover at this level of the code, we should just publish what is given, robustly.  
				{ getEmptyProps(), new byte[0], "Empty envelope and no bytes." }
				};
		return Arrays.asList(data);
	}

	private static BasicProperties getNormalProps() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(RabbitMessageBus.TOPIC_HEADER_KEY, "test.topic");
		headers.put(CUSTOM_HEADER_NAME, "CustomHeaderValue");

		BasicProperties props = new BasicProperties.Builder()
			.messageId(UUID.randomUUID().toString())
			.correlationId(UUID.randomUUID().toString())
			.type("test.event")
			.replyTo("replyto_routingkey")
			.headers(headers)
			.build();
		return props;
	}

	private static BasicProperties getEmptyProps() {
		return new BasicProperties();
	}
	
	private static final String CUSTOM_HEADER_NAME = "CustomHeader";
	
	String testDescription;
	BasicProperties propertiesSent;
	byte[] bytesSent;
	Envelope receivedEnvelope;
	
	public RabbitMessageBus_GetMessageTest(BasicProperties propertiesSent,	byte[] bytesSent, String testDescription) {
		super();
		System.out.println("Test instance: " + testDescription);
		this.testDescription = testDescription;
		this.propertiesSent = propertiesSent;
		this.bytesSent = bytesSent;
	}

	@Before
	@Override
	public void beforeEachTest() throws IOException{
		super.beforeEachTest();

		Connection con = null;
		Channel channel = null;
		try{
			con = connectionFactory.newConnection();
			channel = con.createChannel();
			String queueName = channel.queueDeclare("testQueue", false, false, false, null).getQueue();
			
			channel.basicPublish("", queueName, propertiesSent, bytesSent);
			
			StopWatch timer = new StopWatch();
			timer.start();
			UnacceptedMessage receivedMessage;
			do{
				receivedMessage = rabbitBus.getNextMessageFrom(queueName);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					fail("Thread was interupted");
				}
			}while(receivedMessage == null && timer.getTime() < 2000);
			
			assertNotNull("Message not received in time allowed.", receivedMessage);
			
			receivedEnvelope = receivedMessage.getEnvelope();
			rabbitBus.acceptMessage(receivedMessage);
			
		} finally {
			if(channel != null && channel.isOpen())
				channel.close();
			if(con != null && con.isOpen())
				con.close();
		}
	}
		
	@Test 
	public void getNextMessageShouldRetrieveMessageWithTheSentBytes() throws IOException	{
		assertArrayEquals(bytesSent,  receivedEnvelope.getBody());
	}
	
	@Test 
	public void getNextMessageShouldSetEnvelopeIdBaseOnAmqpIdHeader() throws IOException	{
		if(propertiesSent.getMessageId() == null && receivedEnvelope.getId() == null) return;
		assertEquals(propertiesSent.getMessageId(),  receivedEnvelope.getId().toString());
	}
	
	@Test 
	public void getNextMessageShouldSetEnvelopeCorrelationIdBaseOnAmqpCorrelationIdHeader() throws IOException	{
		if(propertiesSent.getCorrelationId() == null && receivedEnvelope.getCorrelationId() == null) return;
		assertEquals(propertiesSent.getCorrelationId(),  receivedEnvelope.getCorrelationId().toString());
	}
	
	@Test 
	public void getNextMessageShouldSetEnvelopeEventTypeBaseOnAmqpTypeHeader() throws IOException	{
		assertEquals(propertiesSent.getType(),  receivedEnvelope.getEventType());
	}
	
	@Test 
	public void getNextMessageShouldSetEnvelopeReplyToBaseOnAmqpReplyToHeader() throws IOException	{
		assertEquals(propertiesSent.getReplyTo(),  receivedEnvelope.getReplyTo());
	}
	
	@Test 
	public void getNextMessageShouldSetEnvelopeTopicBaseOnCustomHeader() throws IOException	{
		if((propertiesSent.getHeaders() == null || !propertiesSent.getHeaders().containsKey(RabbitMessageBus.TOPIC_HEADER_KEY)) && receivedEnvelope.getTopic() == null) return;
		assertEquals(propertiesSent.getHeaders().get(RabbitMessageBus.TOPIC_HEADER_KEY),  receivedEnvelope.getTopic());
	}
	
	@Test 
	public void getNextMessageShouldSetEnvelopeHeadersBasedOnAmqpHeaders() throws IOException	{
		if((propertiesSent.getHeaders() == null || !propertiesSent.getHeaders().containsKey(CUSTOM_HEADER_NAME)) && !receivedEnvelope.getHeaders().containsKey(CUSTOM_HEADER_NAME)) return;
		assertEquals(propertiesSent.getHeaders().get(CUSTOM_HEADER_NAME), receivedEnvelope.getHeaders().get(CUSTOM_HEADER_NAME));
	}	
	
	@Test 
	public void getNextMessageShouldNotIncludedEnvelopePropertiesSentAsCustomHeadersInEnvelopeHeaders() throws IOException	{
		assertFalse(receivedEnvelope.getHeaders().containsKey(RabbitMessageBus.TOPIC_HEADER_KEY));
	}
}