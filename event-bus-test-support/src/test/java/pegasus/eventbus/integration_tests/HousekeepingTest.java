package pegasus.eventbus.integration_tests;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.testsupport.TestSendEvent;

public class HousekeepingTest extends IntegrationTestBase {

    final Logger LOG = LoggerFactory.getLogger(HousekeepingTest.class);

    public final class TestHandler implements EventHandler<TestSendEvent> {
        private final Logger lOG;

        private TestHandler(Logger lOG) {
            this.lOG = lOG;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends TestSendEvent>[] getHandledEventTypes() {
            Class<?>[] types = { TestSendEvent.class };
            return (Class<? extends TestSendEvent>[]) types;
        }

        @Override
        public EventResult handleEvent(TestSendEvent event) {
            lOG.info("Received event number: " + event.getCount());
            return EventResult.Handled;
        }
    }

    @Test
    public void publishingAnEventShouldCreateExchangeIfMissing() throws HttpException, IOException {

        manager.publish(sendEvent);

        assertExchangeExists();
    }

    @Test
    public void publishingAnEventShouldNotCreateAnyQueues() throws HttpException, IOException {

        manager.publish(sendEvent);

        assertQueueDoesNotExists();
    }

    @Test
    @Ignore("This test has to be run manually.  Its purpose it to verify that publish and subscribe actions can recover from connection failures.  See comments.")
    // To correctly run this test, start test, then stop the RabbitMq server and restart it. Ensure that log shows all events were received in numeric order.
    // Unplugging and plugging network cable will not correctly test this as explained here: http://lists.rabbitmq.com/pipermail/rabbitmq-discuss/2011-September/015329.html
    public void connectivityTest() throws InterruptedException {

        manager.subscribe(new TestHandler(LOG));

        int count = 0;
        while (true) {
            try {
                TestSendEvent event = new TestSendEvent();
                event.setCount(count);
                manager.publish(event);
                count++;
            } catch (Exception e) {
                // LOG.error("Error sending event " + count + ": " + e.getMessage(), e);
            }
            Thread.sleep(1000);
        }
    }

}
