package pegasus.eventbus.amqp;

import org.junit.Ignore;

import pegasus.eventbus.client.EventHandler;

@Ignore("Needs update to conform to use of basicConsume.")
public class AmqpEventManager_NonBlockingRPCSubscribeReceiveAcceptanceTest extends
		AmqpEventManager_EventSubscribeReceiveAcceptanceTestBase {

	@Override
	protected void subscribe(EventHandler<?> eventHandler) {
		manager.getResponseTo(sendEvent, eventHandler);
	}
}