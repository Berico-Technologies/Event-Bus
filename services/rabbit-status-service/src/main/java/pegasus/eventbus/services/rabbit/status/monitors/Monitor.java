package pegasus.eventbus.services.rabbit.status.monitors;

import dashboard.server.metric.Metric;

public interface Monitor{
	Metric getMetric();
}