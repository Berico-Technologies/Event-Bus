package pegasus.eventbus.topology.event;

public class Registration {

    private final String clientName;

    public Registration(String clientName) {
        this.clientName = clientName;
    }
    
    public String getClientName() {
        return clientName;
    }

}
