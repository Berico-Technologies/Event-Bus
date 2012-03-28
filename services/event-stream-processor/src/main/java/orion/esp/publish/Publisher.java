package orion.esp.publish;

public interface Publisher extends Runnable {

    public Publisher setBroker(Broker broker);

    public Publisher setDataProvider(DataProvider dataProvider);
}
