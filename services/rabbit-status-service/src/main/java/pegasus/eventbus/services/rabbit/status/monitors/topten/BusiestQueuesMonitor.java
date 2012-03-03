package pegasus.eventbus.services.rabbit.status.monitors.topten;

import java.util.regex.Pattern;

import pegasus.eventbus.services.rabbit.status.PublisherService;

public class BusiestQueuesMonitor extends TopNMonitor{

	Pattern rateFinder = Pattern.compile("\"deliver_get_details\":\\{\"rate\":(\\d+).*?\"name\":\"(.*?)\"");
	@Override
	protected String getLabel() {
		return "Busiest Queues (Messages Handled)";
	}
	
	@Override
	protected Pattern getRateFinder() {
		return rateFinder;
	}

	@Override
	protected String getJson() {
		return PublisherService.apiHelper.getQueuesJson();
	}
}