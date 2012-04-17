package pegasus.eventbus.topology.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.topology.events.RegisterClient;
import pegasus.eventbus.topology.events.UnregisterClient;

public class ClientRegistry implements Iterable<RegisterClient> {

    protected static final Logger       LOG               = LoggerFactory.getLogger(RegistrationHandler.class);

    private Map<String, RegisterClient> registeredClients = new HashMap<String, RegisterClient>();

    public void registerClient(RegisterClient registerEvent) {
        String clientName = registerEvent.getClientName();

        LOG.trace("Registering client {} in Topology Service.", clientName);

        // if the client is already registered then assume that the client went down and
        // has restarted and is re-registering with the topology service.
        // TODO: is this a safe assumption?  What if by configuration error, two separate 
        // services registered with the same name?  Should we use client name or a UUID?
        if (registeredClients.containsKey(clientName)) {
            registeredClients.remove(clientName);
        }
        registeredClients.put(clientName, registerEvent);
    }

    public void unregisterClient(UnregisterClient unregisterEvent) {
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
