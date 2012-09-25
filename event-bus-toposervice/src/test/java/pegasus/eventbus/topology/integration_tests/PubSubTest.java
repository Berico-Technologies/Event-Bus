package pegasus.eventbus.topology.integration_tests;

import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;

import org.junit.*;

//Enable this class to be integrated as an integration test class
import org.junit.experimental.categories.Category;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

import com.berico.testsupport.IntegrationTest;

@Category(IntegrationTest.class)
public class PubSubTest extends IntegrationTestBase {

    @Ignore
    @Test
    public void canSendAndReceiveEvents() throws Exception {

        TestHandler handler = new TestHandler();
        manager.subscribe(handler);

        manager.publish(new TestEvent());

        waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler).eventWasReceived(), equalTo(true));
    }

    public class TestHandler implements EventHandler<TestEvent> {

        private volatile boolean eventReceived;

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends TestEvent>[] getHandledEventTypes() {
            Class<?>[] handledEvents = { TestEvent.class };
            return (Class<? extends TestEvent>[]) handledEvents;
        }

        @Override
        public EventResult handleEvent(TestEvent event) {
            eventReceived = true;
            return EventResult.Handled;
        }

        public boolean eventWasReceived() {
            return eventReceived;
        }
    }

}
