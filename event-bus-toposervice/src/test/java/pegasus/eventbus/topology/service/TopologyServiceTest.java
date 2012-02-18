package pegasus.eventbus.topology.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import pegasus.eventbus.topology.service.RegistrationHandler;
import pegasus.eventbus.topology.service.TopologyService;

public class TopologyServiceTest {

    private TopologyService     topologyService;
    
    @Mock
    private RegistrationHandler registrationHandler;

    @Mock
    private UnknownEventTypeHandler unknownEventTypeHandler;

    @Before
    public void beforeEachTest() {

        MockitoAnnotations.initMocks(this);

        topologyService = new TopologyService(registrationHandler, unknownEventTypeHandler);
    }

    @Test
    public void testStart() {
        assertNotNull(topologyService);
        topologyService.start();
        verify(registrationHandler).start();
    }

    @Test
    public void stop() {
        assertNotNull(topologyService);
        topologyService.stop();
        verify(registrationHandler).stop();
    }
}
