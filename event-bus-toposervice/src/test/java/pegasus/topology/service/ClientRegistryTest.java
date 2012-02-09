package pegasus.topology.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import pegasus.eventbus.topology.event.RegisterClient;
import pegasus.eventbus.topology.event.UnregisterClient;

public class ClientRegistryTest {

    private static final String ClientName = "clientName";
    private static final String Version    = "1.0";

    private ClientRegistry      clientRegistry;

    @Before
    public void beforeEachTest() {
        clientRegistry = new ClientRegistry();
    }

    @Test
    public void iteratorInitializesNotNull() {
        assertNotNull(clientRegistry.iterator());
    }

    @Test
    public void registeringClientReflectsInIterator() {
        RegisterClient event = new RegisterClient(ClientName, Version);
        clientRegistry.registerClient(event);
        assertEquals(event, clientRegistry.iterator().next());
    }

    @Test
    public void unregisteringClientReflectsInIterator() {
        clientRegistry.registerClient(new RegisterClient(ClientName, Version));
        clientRegistry.unregisterClient(new UnregisterClient(ClientName));
        assertFalse(clientRegistry.iterator().hasNext());
    }

}
