package gov.ment.rabbit.status.monitors.volume;

import java.util.regex.Pattern;

public class PublishedMessagesPerSecondMonitor extends VolumeMetricMonitor {

  Pattern rateFinder = Pattern.compile("\"publish_details\":\\{\"rate\":(\\d+)");

  public PublishedMessagesPerSecondMonitor() {
    super();
  }

  @Override
  protected String getLabel() {
    return "Msg/Sec. Published";
  }

  @Override
  protected Pattern getRateFinder() {
    return rateFinder;
  }
}
