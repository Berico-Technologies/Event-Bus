package pegasus.topology.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.topology.event.RegisterClient;
import pegasus.eventbus.topology.event.UnregisterClient;

public class ClientRegistry implements Iterable<RegisterClient> {

    protected static final Logger       LOG               = LoggerFactory.getLogger(RegistrationHandler.class);

    private Map<String, RegisterClient> registeredClients = new HashMap<String, RegisterClient>();

    public void handleRegisterClientEvent(RegisterClient event) {
        RegisterClient registerEvent = (RegisterClient) event;
        String clientName = registerEvent.getClientName();

        LOG.trace("Registering client {} in Topology Service.", clientName);

        // if the client is already registered then assume that the client went down and
        // has restarted and is re-registering with the topology service.
        if (registeredClients.containsKey(clientName)) {
            registeredClients.remove(clientName);
        }
        registeredClients.put(clientName, registerEvent);
    }

    public void handleUnregisterClientEvent(UnregisterClient event) {
        UnregisterClient unregisterEvent = (UnregisterClient) event;
        String clientName = unregisterEvent.getClientName();

        LOG.trace("Unregistering client {} in Topology Service.", clientName);

        if (registeredClients.containsKey(clientName)) {
            registeredClients.remove(clientName);
        }
    }

    @Override
    public Iterator<RegisterClient> iterator() {
        return registeredClients.values().iterator();
    }

}
