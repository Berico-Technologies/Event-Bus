package pegasus.eventbus.topology.events;

public class HeartBeat extends Registration {

    //@todo - needed for gson in osgi
    public HeartBeat() {
        
    }
    
    public HeartBeat(String clientName) {
        super(clientName);
    }

}