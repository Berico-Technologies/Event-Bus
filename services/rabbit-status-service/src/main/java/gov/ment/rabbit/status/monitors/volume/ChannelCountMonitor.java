package gov.ment.rabbit.status.monitors.volume;

import gov.ment.rabbit.status.PublisherService;

public class ChannelCountMonitor extends VolumeMetricMonitor {

  public ChannelCountMonitor() {
    super();
  }

  @Override
  protected String getLabel() {
    return "Total Channels";
  }

  @Override
  protected int getRate() {
    return PublisherService.apiHelper.getAllChannelNames().size();
  }
}