package pegasus.eventbus.services.rabbit.status.monitors;

import java.util.regex.Pattern;

public class QueuedMessagesMonitor extends VolumeMetricMonitor {
	
	Pattern rateFinder = Pattern.compile("\"queue_totals\":\\{\"messages_ready\":(\\d+)");
	
	public QueuedMessagesMonitor(){
		super();
	}
	
	@Override
	protected String getLabel() {
		return "Total Queued Messages";
	}
	
	protected Pattern getRateFinder() {
		return rateFinder;
	}
}