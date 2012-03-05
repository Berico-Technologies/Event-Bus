package pegasus.eventbus.apis.servicescaffold.events;

public class ServiceResponse {

	protected String serviceId;
	protected String statusMessage;
	protected long timestamp = System.currentTimeMillis();
	
	public ServiceResponse(String serviceName, String statusMessage) {
	
		this.serviceId = serviceName;
		this.statusMessage = statusMessage;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
}
