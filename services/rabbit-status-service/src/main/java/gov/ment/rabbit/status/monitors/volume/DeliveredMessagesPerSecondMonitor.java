package gov.ment.rabbit.status.monitors.volume;

import java.util.regex.Pattern;

public class DeliveredMessagesPerSecondMonitor extends VolumeMetricMonitor {

  Pattern rateFinder = Pattern.compile("\"deliver_get_details\":\\{\"rate\":(\\d+)");

  public DeliveredMessagesPerSecondMonitor() {
    super();
  }

  @Override
  protected String getLabel() {
    return "Msg/Sec. Delivered";
  }

  @Override
  protected Pattern getRateFinder() {
    return rateFinder;
  }
}