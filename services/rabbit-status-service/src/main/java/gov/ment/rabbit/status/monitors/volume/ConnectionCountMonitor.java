package gov.ment.rabbit.status.monitors.volume;

import gov.ment.rabbit.status.PublisherService;

public class ConnectionCountMonitor extends VolumeMetricMonitor {

  public ConnectionCountMonitor() {
    super();
  }

  @Override
  protected String getLabel() {
    return "Total Connections";
  }

  @Override
  protected int getRate() {
    return PublisherService.apiHelper.getAllConnectionNames().size();
  }
}