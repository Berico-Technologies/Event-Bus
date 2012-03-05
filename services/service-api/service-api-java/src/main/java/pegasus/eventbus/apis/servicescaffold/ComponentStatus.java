package pegasus.eventbus.apis.servicescaffold;

import java.util.HashMap;
import java.util.Map;

public class ComponentStatus {

	protected String name;
	protected long startTime = System.currentTimeMillis();
	protected Map<String, String> properties = new HashMap<String, String>();
	protected boolean isRunning = false;
	
	public ComponentStatus(
			String name, 
			long startTime,
			Map<String, String> properties, 
			boolean isRunning) {
		
		this.name = name;
		this.startTime = startTime;
		this.properties = properties;
		this.isRunning = isRunning;
	}

	public String getName() {
		return name;
	}

	public long getStartTime() {
		return startTime;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public boolean isRunning() {
		return isRunning;
	}
	
}
