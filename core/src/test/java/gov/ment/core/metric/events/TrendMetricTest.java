package gov.ment.core.metric.events;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import gov.ment.core.metric.events.TrendMetric;

import org.junit.Before;
import org.junit.Test;

public class TrendMetricTest {

  private TrendMetric metric;

  @Before
  public void beforeEachTest() {
    metric = new TrendMetric();
  }

  @Test
  public void getSetInfo() {
    String expected = "info";
    metric.setInfo(expected);
    assertThat(expected, is(metric.getInfo()));
    metric.setInfo(expected + "2");
    assertThat(expected, not(metric.getInfo()));
  }

  @Test
  public void getSetMax() {
    int expected = 5505;
    metric.setMax(expected);
    assertThat(expected, is(metric.getMax()));
    metric.setMax(expected + 2);
    assertThat(expected, not(metric.getMax()));
  }

  @Test
  public void getSetTrend() {
    int expected = 5696;
    metric.setTrend(expected);
    assertThat(expected, is(metric.getTrend()));
    metric.setTrend(expected + 2);
    assertThat(expected, not(metric.getTrend()));
  }

  @Test
  public void getSetValue() {
    int expected = 5509;
    metric.setValue(expected);
    assertThat(expected, is(metric.getValue()));
    metric.setValue(expected + 2);
    assertThat(expected, not(metric.getValue()));
  }

  @Test
  public void objectInitializesNotNull() {
    assertNotNull(metric);
  }
}
