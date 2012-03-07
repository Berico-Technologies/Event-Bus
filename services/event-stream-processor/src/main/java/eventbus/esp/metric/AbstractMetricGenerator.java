package eventbus.esp.metric;

public abstract class AbstractMetricGenerator implements MetricGenerator {

    protected Broker broker;
    protected DataProvider dataProvider;

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}
