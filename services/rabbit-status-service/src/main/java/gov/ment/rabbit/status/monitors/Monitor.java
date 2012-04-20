package gov.ment.rabbit.status.monitors;

import gov.ment.core.metric.events.Metric;

public interface Monitor {
  Metric getMetric();
}