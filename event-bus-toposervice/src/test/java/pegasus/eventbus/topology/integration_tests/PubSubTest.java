package pegasus.eventbus.topology.integration_tests;

import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;

import org.junit.*;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

@Ignore("Un-ignore after a topology service is actually deployed to the test environment.")
public class PubSubTest extends IntegrationTestBase {

	@Test
	public void canSendAndReceiveEvents() throws Exception {
		
		TestHandler handler = new TestHandler();
		manager.subscribe(handler);
		
		manager.publish(new TestEvent());
		
	    waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler).eventWasReceived(), equalTo(true));
	}
	
    public class TestHandler implements EventHandler<TestEvent>{

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
    	
		public boolean eventWasReceived(){
			return eventReceived;
		}
    }

}
