package pegasus.eventbus.rabbitmq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.experimental.categories.Category;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.core.testsupport.IntegrationTest;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.BasicProperties;

//TODO: PEGA-718 These tests need to be converted from testing getGetMessage to testing Consume
@RunWith(value = Parameterized.class)
@Category(IntegrationTest.class)
public class RabbitMessageBus_GetMessageTest extends RabbitMessageBus_TestBase {

	@Parameters
    public static Collection<Object[]> testDataForReception() {

        byte[] bytesToSend = { 35, 74, 3, 50, 93, 19, 3, 83, 29, 2 };

        Object[][] data = new Object[][] { { getNormalProps(), bytesToSend, "Normal envelope and bytes." },
                // One can argue that an empty envelope is an invalid state, however than any particular property is null is not invalid.
                // Moreover at this level of the code, we should just publish what is given, robustly.
                { getEmptyProps(), new byte[0], "Empty envelope and no bytes." } };
        return Arrays.asList(data);
    }

    private static BasicProperties getNormalProps() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(RabbitMessageBus.TOPIC_HEADER_KEY, "test.topic");
        headers.put(RabbitMessageBus.PUB_TIMESTAMP_HEADER_KEY, 2930830423403L);
        headers.put(CUSTOM_HEADER_NAME, "CustomHeaderValue");

        BasicProperties props = new BasicProperties.Builder()
        	.messageId(UUID.randomUUID().toString())
        	.correlationId(UUID.randomUUID().toString())
        	.type("test.event")
        	.replyTo("replyto_routingkey")
            .timestamp(new Date(2889870423000L))
            .headers(headers)
            .build();
        return props;
    }
    
 
    private static BasicProperties getEmptyProps() {
        return new BasicProperties();
    }

    private static final String CUSTOM_HEADER_NAME = "CustomHeader";

    String                      testDescription;
    BasicProperties             propertiesSent;
    byte[]                      bytesSent;
    Envelope                    receivedEnvelope;

    public RabbitMessageBus_GetMessageTest(BasicProperties propertiesSent, byte[] bytesSent, String testDescription) {
        super();
        System.out.println("Test instance: " + testDescription);
        this.testDescription = testDescription;
        this.propertiesSent = propertiesSent;
        this.bytesSent = bytesSent;
    }

    @Before
    @Override
    public void beforeEachTest() throws IOException {
        super.beforeEachTest();

        EnvelopeHandler consumer = new EnvelopeHandler() {

            @Override
            public EventResult handleEnvelope(Envelope envelope) {
                receivedEnvelope = envelope;

                return EventResult.Handled;
            }

            @Override
            public String getEventSetName() {
                return "";
            }
        };

        String consumerTag = null;
        Channel channel = null;
        try {
            channel = connection.createChannel();
            String queueName = channel.queueDeclare("testQueue", false, false, false, null).getQueue();
            consumerTag = rabbitBus.beginConsumingMessages(queueName, consumer);

            channel.basicPublish("", queueName, propertiesSent, bytesSent);

            StopWatch timer = new StopWatch();
            timer.start();

            while (receivedEnvelope == null && timer.getTime() < 2000) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    fail("Thread was interupted");
                }
            }

            assertNotNull("Message not received in time allowed.", receivedEnvelope);
        } finally {
            rabbitBus.stopConsumingMessages(consumerTag);
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
    }

    @Test
    public void getNextMessageShouldRetrieveMessageWithTheSentBytes() throws IOException {
        assertArrayEquals(bytesSent, receivedEnvelope.getBody());
    }

    @Test
    public void getNextMessageShouldSetEnvelopeIdBaseOnAmqpIdHeader() throws IOException {
        if (propertiesSent.getMessageId() == null && receivedEnvelope.getId() == null)
            return;
        assertEquals(propertiesSent.getMessageId(), receivedEnvelope.getId().toString());
    }

    @Test
    public void getNextMessageShouldSetEnvelopeCorrelationIdBaseOnAmqpCorrelationIdHeader() throws IOException {
        if (propertiesSent.getCorrelationId() == null && receivedEnvelope.getCorrelationId() == null)
            return;
        assertEquals(propertiesSent.getCorrelationId(), receivedEnvelope.getCorrelationId().toString());
    }

    @Test
    public void getNextMessageShouldSetEnvelopeEventTypeBaseOnAmqpTypeHeader() throws IOException {
        assertEquals(propertiesSent.getType(), receivedEnvelope.getEventType());
    }

    @Test
    public void getNextMessageShouldSetEnvelopeReplyToBaseOnAmqpReplyToHeader() throws IOException {
        assertEquals(propertiesSent.getReplyTo(), receivedEnvelope.getReplyTo());
    }

    @Test
    public void getNextMessageShouldSetEnvelopeTimestampBaseOnCustomeHeader() throws IOException {
        if ((propertiesSent.getHeaders() == null || !propertiesSent.getHeaders().containsKey(RabbitMessageBus.PUB_TIMESTAMP_HEADER_KEY)) && receivedEnvelope.getTimestamp() == null)
            return;
        assertEquals(new Date((Long)(propertiesSent.getHeaders().get(RabbitMessageBus.PUB_TIMESTAMP_HEADER_KEY))), receivedEnvelope.getTimestamp());
    }

    @Test
    public void getNextMessageShouldSetEnvelopeTopicBaseOnCustomHeader() throws IOException {
        if ((propertiesSent.getHeaders() == null || !propertiesSent.getHeaders().containsKey(RabbitMessageBus.TOPIC_HEADER_KEY)) && receivedEnvelope.getTopic() == null)
            return;
        assertEquals(propertiesSent.getHeaders().get(RabbitMessageBus.TOPIC_HEADER_KEY), receivedEnvelope.getTopic());
    }

    @Test
    public void getNextMessageShouldSetEnvelopeHeadersBasedOnAmqpHeaders() throws IOException {
        if ((propertiesSent.getHeaders() == null || !propertiesSent.getHeaders().containsKey(CUSTOM_HEADER_NAME)) && !receivedEnvelope.getHeaders().containsKey(CUSTOM_HEADER_NAME))
            return;
        assertEquals(propertiesSent.getHeaders().get(CUSTOM_HEADER_NAME), receivedEnvelope.getHeaders().get(CUSTOM_HEADER_NAME));
    }

    @Test
    public void getNextMessageShouldNotIncludedEnvelopePropertiesSentAsCustomHeadersInEnvelopeHeaders() throws IOException {
        assertFalse("Found header: " + RabbitMessageBus.TOPIC_HEADER_KEY, receivedEnvelope.getHeaders().containsKey(RabbitMessageBus.TOPIC_HEADER_KEY));
        assertFalse("Found header: " + RabbitMessageBus.PUB_TIMESTAMP_HEADER_KEY, receivedEnvelope.getHeaders().containsKey(RabbitMessageBus.PUB_TIMESTAMP_HEADER_KEY));
    }
}
