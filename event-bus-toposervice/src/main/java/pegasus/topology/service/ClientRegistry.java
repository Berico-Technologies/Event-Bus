package pegasus.topology.service;

import java.util.HashSet;
import java.util.Set;

import pegasus.eventbus.topology.event.RegisterClient;
import pegasus.eventbus.topology.event.UnregisterClient;

public class ClientRegistry {

    private Set<String> registeredClients = new HashSet<String>();

    public void handleRegisterClientEvent(RegisterClient event) {
        RegisterClient registerEvent = (RegisterClient) event;
        String clientName = registerEvent.getClientName();
        registeredClients.add(clientName);
    }

    public void handleUnregisterClientEvent(UnregisterClient event) {
        UnregisterClient unregisterEvent = (UnregisterClient) event;
        String clientName = unregisterEvent.getClientName();
        registeredClients.remove(clientName);
    }

}
