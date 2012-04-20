package gov.ment.rabbit.status;

import gov.ment.rabbit.status.monitors.Monitor;
import gov.ment.core.metric.events.Metric;

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
