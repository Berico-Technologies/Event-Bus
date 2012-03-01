package pegasus.eventbus.services.rabbit.status.monitors;

import pegasus.eventbus.services.rabbit.status.PublisherService;

public class QueueCountMonitor extends VolumeMetricMonitor {
	
	public QueueCountMonitor(){
		super();
	}
	
	@Override
	protected String getLabel() {
		return "Total Queues";
	}
	
	@Override
	protected int GetRate() {
		return PublisherService.apiHelper.getAllQueueNames().size();
	}
}