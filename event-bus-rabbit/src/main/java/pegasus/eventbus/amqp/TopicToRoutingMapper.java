package pegasus.eventbus.amqp;

/**
 * Maps a Topic or Named Event Set (string) to a Route (Exchange, Binding, etc).
 * @author Ken Baltrinic (Berico Technologies)
 */
public interface TopicToRoutingMapper {
	
	/**
	 * Given a Topic, return the corresponding Routing Info
	 * @param topic Name of the Topic
	 * @return Route (as a Routing Info object)
	 */
	RoutingInfo getRoutingInfoFor(String topic);

	/**
	 * Given named event set, return an array of Routes.
	 * @param string Named Event Set (name)
	 * @return array of Routes that belong to the Named Event Set.
	 */
	RoutingInfo[] getRoutingInfoForNamedEventSet(String string);
}
