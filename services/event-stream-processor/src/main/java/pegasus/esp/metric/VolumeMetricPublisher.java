package pegasus.esp.metric;

import java.util.Calendar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.esp.AbstractPublisher;
import pegasus.esp.DataProvider;

import dashboard.server.metric.VolumeMetric;

public class VolumeMetricPublisher extends AbstractPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(VolumeMetricPublisher.class);

    public void run() {
        Map<String, Object> data = dataProvider.getData();
        VolumeMetric metric = new VolumeMetric();
        metric.setLabel((String) data.get(DataProvider.LABEL));
        metric.setValue((Integer) data.get(DataProvider.VALUE));
        metric.setTime(Calendar.getInstance().getTimeInMillis());

        LOG.debug("Publishing metric '{}', value: {}.", metric.getLabel(), metric.getValue());

        broker.publish(metric);
    }
}
