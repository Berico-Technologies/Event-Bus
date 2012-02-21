package pegasus.eventbus.topology.event;

public class RegisterClient extends Registration {

    private String version;

    //@todo - needed for gson in osgi
    public RegisterClient() {
        
    }
    
    public RegisterClient(String clientName, String version) {
        super(clientName);
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }

}
