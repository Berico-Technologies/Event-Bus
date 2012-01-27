package pegasus.eventbus.amqp;

public interface TopicToRoutingMapper {
	RoutingInfo getRoutingInfoFor(String topic);

	RoutingInfo[] getRoutingInfoForNamedEventSet(String string);
}
