package pegasus.eventbus.topology.event;

import pegasus.eventbus.amqp.RoutingInfo;

public class EventTypeRoutingInfo {

	final String eventTypeCannonicalName;
	private final RoutingInfo routeInfo;
	
	public EventTypeRoutingInfo(String eventTypeCannonicalName, RoutingInfo routeInfo) {
		super();
		this.eventTypeCannonicalName = eventTypeCannonicalName;
		this.routeInfo = routeInfo;
	}

	public String getEventTypeCannonicalName() {
		return eventTypeCannonicalName;
	}

	public RoutingInfo getRouteInfo() {
		return routeInfo;
	}
}
