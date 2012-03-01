package pegasus.eventbus.services.rabbit.status.monitors;

import java.util.regex.Pattern;

public class PublishedMessagesPerSecondMonitor extends VolumeMetricMonitor {
	
	Pattern rateFinder = Pattern.compile("\"publish_details\":\\{\"rate\":(\\d+)");
	
	public PublishedMessagesPerSecondMonitor(){
		super();
	}
	
	@Override
	protected String getLabel() {
		return "Msg/Sec. Published";
	}
	
	protected Pattern getRateFinder() {
		return rateFinder;
	}
}
