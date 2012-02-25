package pegasus.eventbus.apis.servicescaffold;

import java.util.HashMap;
import java.util.Map;

public class ServiceStatus {

	protected String serviceName;
	protected long startTime = -1l;
	protected long upTime = -1l;
	protected boolean isRunning = false;
	protected Map<String, String> properties = new HashMap<String, String>();
	protected String status = null;
	
	public ServiceStatus(
			String serviceName, long startTime, long upTime, 
			boolean isRunning, Map<String, String> properties, String status) {

		this.serviceName = serviceName;
		this.startTime = startTime;
		this.upTime = upTime;
		this.isRunning = isRunning;
		this.properties = properties;
		this.status = status;
	}

	public String getServiceName(){
		return serviceName;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getUpTime() {
		return upTime;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public String getStatus() {
		return status;
	}
}
