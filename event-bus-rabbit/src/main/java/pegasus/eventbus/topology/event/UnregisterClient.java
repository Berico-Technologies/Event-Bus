package pegasus.eventbus.topology.event;

public class UnregisterClient extends Registration {

    //@todo - needed for gson in osgi
    public UnregisterClient() {
        
    }
    
    public UnregisterClient(String clientName) {
        super(clientName);
    }

}
