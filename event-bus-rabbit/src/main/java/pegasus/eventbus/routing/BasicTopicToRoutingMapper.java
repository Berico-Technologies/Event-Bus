package pegasus.eventbus.routing;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.amqp.TopicToRoutingMapper;

/**
 * A formulaic routing provider that routes all traffic through a single "pegasus" exchange.
 */
public class BasicTopicToRoutingMapper implements TopicToRoutingMapper {

	@Override
	public RoutingInfo getRoutingInfoFor(String topic) {
		return new RoutingInfo("pegasus", RoutingInfo.ExchangeType.Topic, true, topic);
	}

	@Override
	public RoutingInfo[] getRoutingInfoForNamedEventSet(String string) {
		RoutingInfo[] routes = { new RoutingInfo("pegasus", RoutingInfo.ExchangeType.Topic, true, "#") };
		return routes;
	}
}
