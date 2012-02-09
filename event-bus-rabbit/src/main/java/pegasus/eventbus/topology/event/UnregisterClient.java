package pegasus.eventbus.topology.event;

public class UnregisterClient extends Registration {

    public UnregisterClient(String clientName) {
        super(clientName);
    }

}
