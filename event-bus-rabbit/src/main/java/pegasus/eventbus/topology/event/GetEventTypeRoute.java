package pegasus.eventbus.topology.event;

public class GetEventTypeRoute {

	private String eventTypeCannonicalName;

    //@todo - needed for gson in osgi
	public GetEventTypeRoute() {
	    
	}
	
	public GetEventTypeRoute(String eventTypeCannonicalName) {
		super();
		this.eventTypeCannonicalName = eventTypeCannonicalName;
	}

	public String getEventTypeCanonicalName() {
		return eventTypeCannonicalName;
	}
	
	public void setEventTypeCanonicalName(String eventTypeCannonicalName) {
	    this.eventTypeCannonicalName = eventTypeCannonicalName;
	}
	
}
