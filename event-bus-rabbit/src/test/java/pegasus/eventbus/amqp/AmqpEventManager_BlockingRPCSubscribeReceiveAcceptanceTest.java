package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.testsupport.TestResponseEvent;

public class AmqpEventManager_BlockingRPCSubscribeReceiveAcceptanceTest extends AmqpEventManager_TestBase {

    private byte[]            bytesOfResponseMessage = { 1 };
    private TestResponseEvent deserializedResponse   = new TestResponseEvent();

    private void setupResponseMessage() {

        final Envelope responseEnvelope = new Envelope();
        responseEnvelope.setEventType(TestResponseEvent.class.getCanonicalName());
        responseEnvelope.setBody(bytesOfResponseMessage);

        when(serializer.deserialize(bytesOfResponseMessage, TestResponseEvent.class)).thenReturn(deserializedResponse);

        when(messageBus.beginConsumingMessages(anyString(), any(EnvelopeHandler.class))).then(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                EnvelopeHandler handler = (EnvelopeHandler) invocation.getArguments()[1];
                handler.handleEnvelope(responseEnvelope);
                return null;
            }
        });
    }

    @Test
    public void getResponseToShouldReturnTheDeserializedResponse() throws InterruptedException, TimeoutException {

        setupResponseMessage();

        @SuppressWarnings({ "unchecked" })
        TestResponseEvent response = manager.getResponseTo(sendEvent, 100, TestResponseEvent.class);
        assertEquals(deserializedResponse, response);
    }

    @Test
    public void getResponseToShouldReturnTheDeserializedResponseAsSoonAsReceivedNotAtEndOfTimeout() throws InterruptedException, TimeoutException {

        setupResponseMessage();

        StopWatch watch = new StopWatch();
        watch.start();
        @SuppressWarnings({ "unchecked", "unused" })
        TestResponseEvent response = manager.getResponseTo(sendEvent, 1000, TestResponseEvent.class);
        watch.stop();
        assertThat(watch.getTime(), lessThan(100L));
    }

    @Test(expected = TimeoutException.class)
    public void getResponseToShouldThrowIfAResponseIsNotReceivedWithinTheTimeoutPeriod() throws InterruptedException, TimeoutException {

        @SuppressWarnings({ "unchecked", "unused" })
        TestResponseEvent response = manager.getResponseTo(sendEvent, 5, TestResponseEvent.class);

    }

}
