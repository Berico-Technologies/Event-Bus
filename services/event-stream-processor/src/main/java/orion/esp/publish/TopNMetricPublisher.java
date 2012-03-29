package orion.esp.publish;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import dashboard.server.metric.TopNMetric;
import dashboard.server.metric.TrendMetric;

public class TopNMetricPublisher extends AbstractPublisher {

    private static final int TOTAL_METRICS = 10;
    private static final Logger LOG = LoggerFactory.getLogger(TopNMetricPublisher.class);

    @SuppressWarnings("unchecked")
    public void run() {
        long time = Calendar.getInstance().getTimeInMillis();
        Map<String, Object> data = dataProvider.getData();
        TopNMetric metric = new TopNMetric();
        metric.setLabel((String) data.get(DataProvider.LABEL));
        metric.setTime(time);
        for (Map<String, Object> trendData : (List<Map<String, Object>>) data.get(DataProvider.METRICS)) {
            TrendMetric trendMetric = new TrendMetric();
            trendMetric.setLabel((String) trendData.get(DataProvider.LABEL));
            trendMetric.setValue((Integer) trendData.get(DataProvider.VALUE));
            trendMetric.setTrend((Integer) trendData.get(DataProvider.TREND));
            trendMetric.setInfo((String) trendData.get(DataProvider.INFO));
            trendMetric.setTime(time);
            metric.getMetrics().add(trendMetric);
        }
        List<TrendMetric> metrics = metric.getMetrics();
        
		Collections.sort(metrics, new Comparator<TrendMetric>(){
			@Override
			public int compare(TrendMetric arg0, TrendMetric arg1) {
				return arg1.getValue() - arg0.getValue();
			}});

		if(metrics.size() > TOTAL_METRICS){
			int leastValueToReturn = metrics.get(10 -1).getValue();
			for(int i = metrics.size()-1; metrics.get(i).getValue() < leastValueToReturn; i--){
				metrics.remove(i);
			}
		}

		LOG.debug("Publishing metric '{}'.", metric.getLabel());

        broker.publish(metric);
    }
}
