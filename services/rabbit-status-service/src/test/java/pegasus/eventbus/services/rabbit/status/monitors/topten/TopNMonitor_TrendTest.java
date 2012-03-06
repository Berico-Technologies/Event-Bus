package pegasus.eventbus.services.rabbit.status.monitors.topten;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import dashboard.server.metric.TopNMetric;
import dashboard.server.metric.TrendMetric;

public class TopNMonitor_TrendTest {

	private static TopNMetric originalMetric;
	private static TrendMetric[] secondMetric;
	
	@BeforeClass
	public static void onceBeforeAllTests(){
		TestMonitor monitor = new TestMonitor();
		
		originalMetric = (TopNMetric) monitor.getMetric();
		List<TrendMetric> secondMetrics = ((TopNMetric) monitor.getMetric()).getMetrics();
		Collections.sort(secondMetrics, 
				new Comparator<TrendMetric>(){
					@Override
					public int compare(TrendMetric arg0, TrendMetric arg1) {
						return arg0.getLabel().compareTo(arg1.getLabel());
				}});
		
		secondMetric = secondMetrics.toArray(new TrendMetric[0]);
		
	}
	
	@Test
	public void metricsWithNoPriorMetricShouldReturnATrentOfZero() {
		for(TrendMetric metric : originalMetric.getMetrics()){
			assertEquals(metric.getLabel(), 0, metric.getTrend());
		}
	}
	
	@Test
	public void metricWithPriorValueOfZeroShouldReturnATrendOfZero(){
		assertEquals(secondMetric[0].getLabel(), 0, secondMetric[0].getTrend());
	}
	
	@Test
	public void metricWithCurrentValueEqualToThePriorValueShouldReturnATrendOfZero(){
		assertEquals(secondMetric[1].getLabel(), 0, secondMetric[1].getTrend());
	}
	
	@Test
	public void metricWithCurrentValueDoubleThePriorValueShouldReturnATrendOf100Percent(){
		assertEquals(secondMetric[2].getLabel(), 100, secondMetric[2].getTrend());
	}
	
	@Test
	public void metricWithCurrentValueHalfThePriorValueShouldReturnATrendOf50Percent(){
		assertEquals(secondMetric[3].getLabel(), -50, secondMetric[3].getTrend());
	}
	
	@Test
	public void metricWithANegativeCurrentValueAndAPositivePriorValueShouldReturnANegativeTrend(){
		assertEquals(secondMetric[4].getLabel(), -200, secondMetric[4].getTrend());
	}
	
	@Test
	public void metricWithAPostiveCurrentValueAndANegativePriorValueShouldReturnAPostiveTrend(){
		assertEquals(secondMetric[5].getLabel(), 200, secondMetric[5].getTrend());
	}

	private static class TestMonitor extends TopNMonitor{

		@Override
		protected String getLabel() {
			return null;
		}

		@Override
		protected String getJson() {
			return null;
		}

		@Override
		protected Pattern getRateFinder() {
			return null;
		}
		
		boolean initialMetricSetGenerated = false;

		@Override
		protected MetricSource getMetricSource() {
			
			if(initialMetricSetGenerated){
				return new TestSource(1, 100, 200, 50, -100, 100);
			} else {
				initialMetricSetGenerated = true;
				return new TestSource(0, 100, 100, 100, 100, -100);
			}
		}
		
		private class TestSource implements MetricSource{

			private final int[] values;
			private int index = 0;
			public TestSource(int... values){
				this.values = values;
			}
			
			@Override
			public TrendMetric getNextMetricGreaterThan(int threshold) {
				if(index == values.length) return null;
				
				TrendMetric metric = new TrendMetric();
				metric.setValue(values[index]);
				metric.setLabel("#" + index);
				index++;
				return metric;
			}
			
		}
	}
	
}
