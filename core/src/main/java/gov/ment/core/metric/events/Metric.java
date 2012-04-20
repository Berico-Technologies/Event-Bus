package gov.ment.core.metric.events;

public class Metric {

  protected String label;
  protected long time;
  protected String type;

  public Metric() {

  }

  public String getLabel() {
    return label;
  }

  public long getTime() {
    return time;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
