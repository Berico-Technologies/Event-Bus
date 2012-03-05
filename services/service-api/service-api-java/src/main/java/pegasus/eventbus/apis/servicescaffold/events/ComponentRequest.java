package pegasus.eventbus.apis.servicescaffold.events;

import java.util.HashMap;
import java.util.Map;

public class ComponentRequest {

	public enum Action {
		Start,
		Stop,
		Uninstall,
		Status,
		List
	}
	
	protected String serviceIdOrPattern;
	protected String componentIdOrPattern;
	protected Action action;
	protected Map<String, String> options = new HashMap<String, String>();
	
	public ComponentRequest(String serviceNamePattern, String componentNamePattern, Action action) {
		
		this.serviceIdOrPattern = serviceNamePattern;
		this.componentIdOrPattern = componentNamePattern;
		this.action = action;
	}
	
	public ComponentRequest(String serviceNamePattern, String componentNamePattern,
			Action action, Map<String, String> options) {
		
		this.serviceIdOrPattern = serviceNamePattern;
		this.componentIdOrPattern = componentNamePattern;
		this.action = action;
		this.options = options;
	}

	public String getServiceIdOrPattern() {
		return serviceIdOrPattern;
	}

	public String getComponentIdOrPattern() {
		return componentIdOrPattern;
	}

	public Action getAction() {
		return action;
	}

	public Map<String, String> getOptions() {
		return options;
	}
}
