package pegasus.eventbus.amqp;

import pegasus.eventbus.client.EventHandler;

public class AmqpEventManager_NonBlockingRPCSubscribeReceiveAcceptanceTest extends
		AmqpEventManager_EventSubscribeReceiveAcceptanceTestBase {

	@Override
	protected void subscribe(EventHandler<?> eventHandler) {
		manager.getResponseTo(sendEvent, eventHandler);
	}
}