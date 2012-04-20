package gov.ment.core.metric.events;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import gov.ment.core.metric.events.TopNMetric;
import gov.ment.core.metric.events.TrendMetric;

import org.junit.Before;
import org.junit.Test;

public class TopNMetricTest {

  private TopNMetric metric;

  @Before
  public void beforeEachTest() {
    metric = new TopNMetric();
  }

  @SuppressWarnings("serial")
  @Test
  public void getSetMetrics() {
    List<TrendMetric> expected = new ArrayList<TrendMetric>() {
      {
        add(new TrendMetric());
        add(new TrendMetric());
      }
    };
    metric.setMetrics(expected);
    assertThat(expected, is(metric.getMetrics()));
    metric.setMetrics(new ArrayList<TrendMetric>() {
      {
        add(new TrendMetric());
        add(new TrendMetric());
      }
    });
    assertThat(expected, not(metric.getMetrics()));
  }

  @Test
  public void objectInitializesNotNull() {
    assertNotNull(metric);
  }
}
