package gov.ment.core.metric.events;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import gov.ment.core.metric.events.Metric;

import org.junit.Before;
import org.junit.Test;

public class MetricTest {

  private Metric metric;

  @Before
  public void beforeEachTest() {
    metric = new Metric();
  }

  @Test
  public void getSetLabel() {
    String expected = "label";
    metric.setLabel(expected);
    assertThat(expected, is(metric.getLabel()));
    metric.setLabel(expected + "2");
    assertThat(expected, not(metric.getLabel()));
  }

  @Test
  public void getSetTime() {
    long expected = 512335609L;
    metric.setTime(expected);
    assertThat(expected, is(metric.getTime()));
    metric.setTime(expected + 2);
    assertThat(expected, not(metric.getTime()));
  }

  @Test
  public void objectInitializesNotNull() {
    assertNotNull(metric);
  }
}
