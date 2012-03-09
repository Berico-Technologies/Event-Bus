package pegasus.eventbus.testsupport;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.amqp.TopologyManager;
import pegasus.eventbus.client.EventManager;

public class TopologyManagerForIntegrationTests implements TopologyManager {

	@Override
	public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {
		return new RoutingInfo("pegasus-test", RoutingInfo.ExchangeType.Topic, true, eventType.getCanonicalName());
	}

	@Override
	public RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName) {
		RoutingInfo[] routes = { new RoutingInfo("pegasus-test", RoutingInfo.ExchangeType.Topic, true, "#") };
		return routes;
	}

	@Override
	public void start(EventManager eventManager) {
		
	}

	@Override
	public void close() {

	}

}
