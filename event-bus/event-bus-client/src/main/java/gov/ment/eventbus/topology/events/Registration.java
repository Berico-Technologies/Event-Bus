package gov.ment.eventbus.topology.events;

public class Registration {

  private String clientName;

  // @todo - needed for gson in osgi
  public Registration() {

  }

  public Registration(String clientName) {
    this.clientName = clientName;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

}
