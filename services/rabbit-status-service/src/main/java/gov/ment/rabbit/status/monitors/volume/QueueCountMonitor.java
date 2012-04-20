package gov.ment.rabbit.status.monitors.volume;

import gov.ment.rabbit.status.PublisherService;

public class QueueCountMonitor extends VolumeMetricMonitor {

  public QueueCountMonitor() {
    super();
  }

  @Override
  protected String getLabel() {
    return "Total Queues";
  }

  @Override
  protected int getRate() {
    return PublisherService.apiHelper.getAllQueueNames().size();
  }
}