package gov.ment.core.metric.events;

public class TrendMetric extends Metric {

  public static final String TREND = "trend";

  protected int value;
  protected int trend;
  protected String info;
  protected int max;

  public TrendMetric() {
    super();
    type = TREND;
  }

  public String getInfo() {
    return info;
  }

  public int getMax() {
    return max;
  }

  public int getTrend() {
    return trend;
  }

  public int getValue() {
    return value;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public void setTrend(int trend) {
    this.trend = trend;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
