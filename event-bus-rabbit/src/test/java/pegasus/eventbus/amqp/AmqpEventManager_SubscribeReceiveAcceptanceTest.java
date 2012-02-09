package pegasus.eventbus.amqp;

import pegasus.eventbus.client.EventHandler;

public class AmqpEventManager_SubscribeReceiveAcceptanceTest extends
		AmqpEventManager_EventSubscribeReceiveAcceptanceTestBase {

	@Override
	protected void subscribe(EventHandler<?> eventHandler) {
		manager.subscribe(eventHandler);
	}
}
