package pegasus.eventbus.rabbitmq;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pegasus.cip.core.testsupport.IntegrationTest;
import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventResult;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

@Category(IntegrationTest.class)
public class RabbitMessageBus_MessageAcknowledgementTest extends RabbitMessageBus_TestBase {

    @Mock
    protected EnvelopeHandler consumer;

    private enum ResponseType {
        Accept, RejectNoRetry, RejectWithRetry, DoNotResponsd
    }

    private String queueName = UUID.randomUUID().toString();

    private void sendMessagesAndRespondToItWith(ResponseType responseType) throws IOException {

        Channel channel = null;
        String consumerTag = null;
        try {
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null).getQueue();
            consumerTag = rabbitBus.beginConsumingMessages(queueName, consumer);

            switch (responseType) {
                case Accept:
                    when(consumer.handleEnvelope(any(Envelope.class))).thenReturn(EventResult.Handled);
                    break;
                case RejectNoRetry:
                    when(consumer.handleEnvelope(any(Envelope.class))).thenReturn(EventResult.Failed);
                    break;
                case RejectWithRetry:
                    when(consumer.handleEnvelope(any(Envelope.class))).thenReturn(EventResult.Retry);
                    break;
                case DoNotResponsd:
                    // do nothing
                    when(consumer.handleEnvelope(any(Envelope.class))).then(new Answer<String>() {

                        @Override
                        public String answer(InvocationOnMock invocation) throws Throwable {
                            // sleep longer than timer to simulate no message

                            StopWatch timer = new StopWatch();
                            timer.start();

                            while (timer.getTime() < 2000) {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    fail("Thread was interupted");
                                }
                            }
                            return null;
                        }

                    });
                    break;
            }

            channel.basicPublish("", queueName, null, new byte[0]);

            StopWatch timer = new StopWatch();
            timer.start();

            while (timer.getTime() < 1000) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    fail("Thread was interupted");
                }
            }
        } finally {
            rabbitBus.stopConsumingMessages(consumerTag);
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
    }

    private boolean queueIsEmpty() throws IOException {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            GetResponse message = channel.basicGet(queueName, false);
            return message == null;
        } finally {
            if (channel != null && channel.isOpen())
                channel.close();
        }
    }

    @Before
    public void beforeEachTest() throws IOException {
        super.beforeEachTest();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void anAcceptedMessageShouldNotBeReturnedToTheQueue() throws IOException {
        sendMessagesAndRespondToItWith(ResponseType.Accept);
        assertTrue(queueIsEmpty());
    }

    @Test
    public void aRejectedMessageWithoutResendNotBeReturnedToTheQueue() throws IOException {
        sendMessagesAndRespondToItWith(ResponseType.RejectNoRetry);
        assertTrue(queueIsEmpty());
    }

    @Test
    public void aRejectedMessageWithResendShouldBeReturnedToTheQueue() throws IOException {
        sendMessagesAndRespondToItWith(ResponseType.RejectWithRetry);
        assertFalse(queueIsEmpty());
    }

    @Test
    public void aMessageThatIsNeitherAcceptedNorRejectedIsReturnedToTheQueueUponChannelClose() throws IOException {
        // In other word, this test asserts that we are not auto-acknowledging messages.
        sendMessagesAndRespondToItWith(ResponseType.DoNotResponsd);
        rabbitBus.close();
        assertFalse(queueIsEmpty());
    }
}
