package gov.ment.esp.publish;

public abstract class AbstractPublisher implements Publisher {

  protected Broker broker;
  protected DataProvider dataProvider;

  public Publisher setBroker(Broker broker) {
    this.broker = broker;
    return this;
  }

  public Publisher setDataProvider(DataProvider dataProvider) {
    this.dataProvider = dataProvider;
    return this;
  }
}
