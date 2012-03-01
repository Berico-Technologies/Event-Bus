package pegasus.eventbus.services.rabbit.status.monitors;

import pegasus.eventbus.services.rabbit.status.PublisherService;

public class ConnectionCountMonitor extends VolumeMetricMonitor {
	
	public ConnectionCountMonitor(){
		super();
	}
	
	@Override
	protected String getLabel() {
		return "Total Connections";
	}
	
	@Override
	protected int getRate() {
		return PublisherService.apiHelper.getAllConnectionNames().size();
	}
}