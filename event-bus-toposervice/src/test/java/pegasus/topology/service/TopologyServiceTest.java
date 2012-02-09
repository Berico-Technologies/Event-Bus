package pegasus.topology.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TopologyServiceTest {
    
    private TopologyService topologyService;
    @Mock
    private RegistrationHandler registrationHandler;
    
    @Before
    public void beforeEachTest() {

        MockitoAnnotations.initMocks(this);

        topologyService = new TopologyService(registrationHandler);
    }
    
    @Test
    public void testStart() {
        topologyService.start();
        verify(registrationHandler).start();
    }

    @Test
    public void stop() {
        topologyService.stop();
        verify(registrationHandler).stop();
    }
}
