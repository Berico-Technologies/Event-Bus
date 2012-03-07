package eventbus.esp.metric;

public interface MetricGenerator extends Runnable {

    public void setBroker(Broker broker);

    public void setDataProvider(DataProvider dataProvider);
}
