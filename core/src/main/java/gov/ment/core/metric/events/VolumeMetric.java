package gov.ment.core.metric.events;

public class VolumeMetric extends Metric {

  public static final String VOLUME = "volume";

  protected int value;

  public VolumeMetric() {
    super();
    type = VOLUME;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
