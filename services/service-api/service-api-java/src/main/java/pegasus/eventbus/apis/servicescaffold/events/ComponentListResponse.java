package pegasus.eventbus.apis.servicescaffold.events;

import java.util.ArrayList;
import java.util.Collection;

import pegasus.eventbus.apis.servicescaffold.ComponentStatus;

public class ComponentListResponse {

	protected String serviceId;
	protected long timestamp = System.currentTimeMillis();
	
	protected ArrayList<ComponentStatus> componentList = new ArrayList<ComponentStatus>();
	
	public ComponentListResponse(
		String serviceId){
		
		this.serviceId = serviceId;
	}
	
	public ComponentListResponse(
		String serviceId, Collection<ComponentStatus> componentStatuses){
		
		this.serviceId = serviceId;
		this.componentList.addAll(componentStatuses);
	}

	public void addComponentStatus(ComponentStatus componentStatus){
		this.componentList.add(componentStatus);
	}
	
	public ArrayList<ComponentStatus> getComponentList() {
		return componentList;
	}

	public String getServiceId() {
		return serviceId;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
