package pegasus.esp;

public interface Publisher extends Runnable {

    public void setBroker(Broker broker);

    public void setDataProvider(DataProvider dataProvider);
}
