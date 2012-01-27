package pegasus.eventbus.amqp;

import pegasus.eventbus.testsupport.TestResponseEvent;

public class AmqpEventManager_NonBlockingRPCHousekeepingTest extends
AmqpEventManager_PublishHousekeepingTestBase {

	TestEventHandler handler = new TestEventHandler(TestResponseEvent.class);
	
	@Override
	protected void publish() {
		manager.getResponseTo(sendEvent, handler);
	}
}