package eventbus.esp.metric;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dashboard.server.metric.TopNMetric;
import dashboard.server.metric.TrendMetric;
import eventbus.esp.metric.AbstractMetricGenerator;
import eventbus.esp.metric.DataProvider;

public class TopNMetricGenerator extends AbstractMetricGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TopNMetricGenerator.class);

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

        LOG.debug("Publishing metric '{}'.", metric.getLabel());

        broker.publish(metric);
    }
}
