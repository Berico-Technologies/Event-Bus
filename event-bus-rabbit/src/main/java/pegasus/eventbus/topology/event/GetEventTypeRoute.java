package pegasus.eventbus.topology.event;

public class GetEventTypeRoute {

	final String eventTypeCannonicalName;
	
	public GetEventTypeRoute(String eventTypeCannonicalName) {
		super();
		this.eventTypeCannonicalName = eventTypeCannonicalName;
	}

	public String getEventTypeCanonicalName() {
		return eventTypeCannonicalName;
	}
}
