package gov.ment.rabbit.status.monitors.volume;

import java.util.regex.Pattern;

public class QueuedMessagesMonitor extends VolumeMetricMonitor {

  Pattern rateFinder = Pattern.compile("\"queue_totals\":\\{.*?\"messages_ready\":(\\d+)");

  public QueuedMessagesMonitor() {
    super();
  }

  @Override
  protected String getLabel() {
    return "Total Queued Messages";
  }

  @Override
  protected Pattern getRateFinder() {
    return rateFinder;
  }
}