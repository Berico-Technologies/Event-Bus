package eventbus.esp.metric;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricPublisherService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricPublisherService.class);

    private static final int PERIOD = 1000;
    private static final TimeUnit UNIT = TimeUnit.MILLISECONDS;
    private static final int POOL_SIZE = 10;

    private ScheduledExecutorService scheduler;
    private List<MetricGenerator> metricGenerators;

    public MetricPublisherService() {

    }

    public void setMetricGenerators(List<MetricGenerator> metricGenerators) {
        this.metricGenerators = metricGenerators;
    }

    public void start() {

        LOG.info("MetricPublisher Service starting...");

        if (scheduler == null) {
            int delay = 0;
            int intervalBetweenMonitors = PERIOD / metricGenerators.size();
            scheduler = Executors.newScheduledThreadPool(POOL_SIZE);
            for (MetricGenerator metricGenerator : metricGenerators) {
                scheduler.scheduleAtFixedRate(metricGenerator, delay, PERIOD, UNIT);
                delay += intervalBetweenMonitors;
            }
        }

        LOG.info("MetricPublisher Service started.");
    }

    public void stop() {

        LOG.info("MetricPublisher Service stopping...");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        LOG.info("MetricPublisher Service stopped.");
    }
}
