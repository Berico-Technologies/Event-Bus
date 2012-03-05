package pegasus.eventbus.apis.servicescaffold.events;

public class ServiceRequest {

	public enum Action {
		Start,
		Stop,
		Status
	}
	
	protected String serviceIdOrPattern;
	protected Action action;

	public ServiceRequest(String name, Action action) {

		this.serviceIdOrPattern = name;
		this.action = action;
	}

	public String getServiceIdOrPattern() {
		return serviceIdOrPattern;
	}

	public Action getAction() {
		return action;
	}

}