package gov.ment.core.metric.events;

import java.util.ArrayList;
import java.util.List;

public class TopNMetric extends Metric {

  public static final String TOPN = "top n";

  protected List<TrendMetric> metrics = new ArrayList<TrendMetric>();

  public TopNMetric() {
    super();
    type = TOPN;
  }

  public List<TrendMetric> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<TrendMetric> metrics) {
    this.metrics = metrics;
  }
}
