package pegasus.eventbus.services.rabbit.status.monitors;

import java.util.regex.Pattern;

public class UnacknowledgedMessagesMonitor extends VolumeMetricMonitor {
	
	Pattern rateFinder = Pattern.compile("\"queue_totals\":\\{\"messages_unacknowledged\":(\\d+)");
	
	public UnacknowledgedMessagesMonitor(){
		super();
	}
	
	@Override
	protected String getLabel() {
		return "Total Unacknowledged Messages";
	}
	
	@Override
	protected Pattern getRateFinder() {
		return rateFinder;
	}
}