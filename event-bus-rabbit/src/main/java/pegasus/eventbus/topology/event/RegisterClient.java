package pegasus.eventbus.topology.event;

public class RegisterClient extends Registration {

    private final String version;

    public RegisterClient(String clientName, String version) {
        super(clientName);
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

}
