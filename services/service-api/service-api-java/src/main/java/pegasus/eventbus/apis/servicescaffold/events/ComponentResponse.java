package pegasus.eventbus.apis.servicescaffold.events;

public class ComponentResponse {

	protected String serviceId;
	protected String componentId;
	protected String statusMessage;
	protected long timestamp = System.currentTimeMillis();
	
	public ComponentResponse(String serviceId, String componentId,
			String statusMessage) {
		
		this.serviceId = serviceId;
		this.componentId = componentId;
		this.statusMessage = statusMessage;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getComponentId() {
		return componentId;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
