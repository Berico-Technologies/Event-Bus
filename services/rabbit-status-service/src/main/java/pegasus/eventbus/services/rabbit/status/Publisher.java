package pegasus.eventbus.services.rabbit.status;

import dashboard.server.metric.Metric;
import pegasus.eventbus.services.rabbit.status.monitors.Monitor;

public class Publisher implements Runnable {

	private final Monitor monitor;
	
	public Publisher(Monitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void run() {

		Metric metric = monitor.getMetric();
		PublisherService.eventManager.publish(metric);

	}

}
