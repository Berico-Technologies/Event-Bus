package eventbus.esp.metric;

import java.util.Calendar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dashboard.server.metric.VolumeMetric;
import eventbus.esp.metric.AbstractMetricGenerator;
import eventbus.esp.metric.DataProvider;

public class VolumeMetricGenerator extends AbstractMetricGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(VolumeMetricGenerator.class);

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
