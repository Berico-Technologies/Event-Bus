package pegasus.eventbus.services.rabbit.status.monitors;

import pegasus.eventbus.services.rabbit.status.PublisherService;

public class ChannelCountMonitor extends VolumeMetricMonitor {
	
	public ChannelCountMonitor(){
		super();
	}
	
	@Override
	protected String getLabel() {
		return "Total Channels";
	}
	
	@Override
	protected int GetRate() {
		return PublisherService.apiHelper.getAllChannelNames().size();
	}
}