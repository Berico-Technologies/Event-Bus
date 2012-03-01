package pegasus.eventbus.services.rabbit.status.monitors;

import java.util.regex.Pattern;

public class TotalMessagesMonitor extends VolumeMetricMonitor {
	
	Pattern rateFinder = Pattern.compile("\"queue_totals\":\\{\"messages\":(\\d+)");
	
	public TotalMessagesMonitor(){
		super();
	}
	
	@Override
	protected String getLabel() {
		return "Total Unacknowledged + Queued Messages";
	}
	
	protected Pattern getRateFinder() {
		return rateFinder;
	}
}