package pegasus.esp;

public abstract class AbstractPublisher implements Publisher {

    protected Broker broker;
    protected DataProvider dataProvider;

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}
