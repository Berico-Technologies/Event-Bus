package pegasus.eventbus.amqp;

import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.*;

import pegasus.eventbus.testsupport.TestSendEvent;

/**
 * Test fixture so assert that calling AmqpEventManger.Publish creates exchange, etc. as needed.
 */
public abstract class AmqpEventManager_PublishHousekeepingTestBase extends AmqpEventManager_TestBase {

    protected TestSendEvent sendEvent;

    protected byte[]        bytesFromSerializer = { 39, 84, 72, 30, 87, 50, 98, 75, 0 };

    @Before
    @Override
    public void beforeEachTest() {

        super.beforeEachTest();

        sendEvent = new TestSendEvent("John Doe", new Date(), 101, "weather", "wind", "age");

        when(topologyManager.getRoutingInfoForEvent(TestSendEvent.class)).thenReturn(routingInfo);

        when(serializer.serialize(sendEvent)).thenReturn(bytesFromSerializer);

        publish();

    }

    protected abstract void publish();

    @Test
    public void publishingAnEventShouldCreateExchange() {

        verify(messageBus).createExchange(routingInfo.getExchange());
    }

    @Test
    public void publishingMultipleEventsToTheSameExchangeShouldNotCreateExchangeMoreThanOnce() {

        publish();

        verify(messageBus, times(1)).createExchange(routingInfo.getExchange());
    }
}
