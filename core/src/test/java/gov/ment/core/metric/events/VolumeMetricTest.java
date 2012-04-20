package gov.ment.core.metric.events;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import gov.ment.core.metric.events.VolumeMetric;

import org.junit.Before;
import org.junit.Test;

public class VolumeMetricTest {

  private VolumeMetric metric;

  @Before
  public void beforeEachTest() {
    metric = new VolumeMetric();
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
