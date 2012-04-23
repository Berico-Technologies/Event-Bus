package pegasus.eventbus.topology.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Iterator;

import pegasus.eventbus.topology.events.RegisterClient;
import pegasus.eventbus.topology.events.UnregisterClient;
import pegasus.eventbus.topology.service.ClientRegistry;

public class ClientRegistryTest {

    private ClientRegistry   clientRegistry;
    @Mock
    private RegisterClient   registerEvent;
    @Mock
    private UnregisterClient unregisterEvent;

    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);

        clientRegistry = new ClientRegistry();
    }

    @Test
    public void iteratorInitializesNotNull() {
        assertNotNull(clientRegistry.iterator());
    }

    @Test
    public void registeringClientReflectsInIterator() {
        clientRegistry.registerClient(registerEvent);
        assertEquals(registerEvent, clientRegistry.iterator().next());
    }

    @Test
    public void unregisteringClientReflectsInIterator() {
        String clientName = "clientName";
        String version = "1.0";
        clientRegistry.registerClient(new RegisterClient(clientName, version));
        clientRegistry.unregisterClient(new UnregisterClient(clientName));
        assertFalse(clientRegistry.iterator().hasNext());
    }

    @Test
    public void reregisteringClientOverwritesInIterator() {
        String clientName = "clientName";
        String version = "1.0";
        RegisterClient registerEvent1 = new RegisterClient(clientName, version);
        RegisterClient registerEvent2 = new RegisterClient(clientName, version);
        clientRegistry.registerClient(registerEvent1);
        clientRegistry.registerClient(registerEvent2);
        Iterator<RegisterClient> iterator = clientRegistry.iterator();
        assertEquals(registerEvent2, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void unregisteringNonexistantClientDoesntError() {
        clientRegistry.unregisterClient(unregisterEvent);
        assertNotNull(clientRegistry);
    }

}
